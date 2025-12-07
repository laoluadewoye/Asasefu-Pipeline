package com.laoluade.ingestor.ao3.core;

// Core Classes
import com.laoluade.ingestor.ao3.errors.*;

// Server Model Classes
import com.laoluade.ingestor.ao3.models.ArchiveIngestorResponse;
import com.laoluade.ingestor.ao3.models.ArchiveIngestorSession;
import com.laoluade.ingestor.ao3.models.ArchiveIngestorSessionInfo;
import com.laoluade.ingestor.ao3.services.ArchiveIngestorLogManager;
import com.laoluade.ingestor.ao3.services.ArchiveIngestorSessionManager;

// JSON Classes
import org.json.JSONArray;
import org.json.JSONObject;

// Selenium Classes
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.WebDriverWait;

// Spring Boot Classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// I/O Classes
import java.io.IOException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

// Structure Classes
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.Duration;

@Component
public class ArchiveIngestor {
    // Public Class Constants
    public static final String VERSION = "0.1";
    public static final String PLACEHOLDER = "Null";
    public static final ArrayList<String> PARAGRAPH_IGNORE_TAGS = new ArrayList<>(Arrays.asList(
            "strong", "em", "u", "span"
    ));

    // Private Class Constants
    private static final Integer TOS_SLEEP_DURATION_SECS = 3;
    private static final Integer WAIT_DURATION_SECS = 5;
    private static final Integer MAX_THREAD_DEPTH = 10;
    private static final String ALL_CHILDREN_XPATH = ".//*";
    private static final String DIRECT_CHILDREN_XPATH = "./*";

    // Instance Constants
    public JSONObject storyLinks;
    public JSONObject versionTable;

    // Spring Boot attributes
    @Autowired
    private final ArchiveIngestorLogManager logManager;

    @Autowired
    private final ArchiveIngestorSessionManager sessionManager;

    public ArchiveIngestor() throws IOException {
        System.out.println("Creating new Archive Ingestor...");

        System.out.println("Loading story links...");
        this.storyLinks = getJSONFromResource("story_links.json");

        System.out.println("Loading version table...");
        this.versionTable = getJSONFromResource("version_table.json");

        this.logManager = null;
        this.sessionManager = null;
    }

    public ArchiveIngestor(ArchiveIngestorLogManager logManager, ArchiveIngestorSessionManager sessionManager)
            throws IOException {
        System.out.println("Creating new Archive Ingestor...");

        System.out.println("Loading story links...");
        this.storyLinks = getJSONFromResource("story_links.json");

        System.out.println("Loading version table...");
        this.versionTable = getJSONFromResource("version_table.json");

        System.out.println("Linking to existing logger...");
        this.logManager = logManager;

        System.out.println("Linking to existing session manager...");
        this.sessionManager = sessionManager;
    }

    public static JSONObject getJSONFromFilepath(String filePath) throws IOException {
        FileReader reader = new FileReader(filePath);
        String jsonString = reader.readAllAsString();
        return new JSONObject(jsonString);
    }

    private JSONObject getJSONFromResource(String resource) throws IOException {
        InputStream resourceStream = ArchiveIngestor.class.getResourceAsStream(resource);
        assert resourceStream != null;
        BufferedReader resourceReader = new BufferedReader(new InputStreamReader(resourceStream));
        String resourceString = resourceReader.readAllAsString();
        return new JSONObject(resourceString);
    }

    public String getArchiveIngestorVersion() {
        return ArchiveIngestor.VERSION;
    }

    public String getOTWArchiveVersion() {
        JSONArray supportedOTWArchiveVersions = this.versionTable.getJSONArray(ArchiveIngestor.VERSION);
        return supportedOTWArchiveVersions.toList().getLast().toString();
    }

    private void updateLastMessage(String newMessage, String sessionID) {
        // Update last message and timestamp
        if (this.sessionManager != null) {
            ArchiveIngestorSession curSession = this.sessionManager.getSession(sessionID);
            ArchiveIngestorResponse curResponse = curSession.getSessionResponse();
            ArchiveIngestorSessionInfo curSessionInfo = curResponse.getSessionInfo();

            curSessionInfo.setLastMessage(newMessage);
            curSessionInfo.refreshCreationTimestamp();

            curResponse.setSessionInfo(curSessionInfo);
            this.sessionManager.updateSession(sessionID, curResponse);

            this.logManager.createInfoLog(newMessage);
        }

        // Print the new message
        System.out.println(newMessage);
    }

    private void updateTotalChapters(Integer chapterCount, String sessionID) {
        if (this.sessionManager != null) {
            ArchiveIngestorSession curSession = this.sessionManager.getSession(sessionID);
            ArchiveIngestorResponse curResponse = curSession.getSessionResponse();
            ArchiveIngestorSessionInfo curSessionInfo = curResponse.getSessionInfo();

            curSessionInfo.setChaptersTotal(chapterCount);
            curSessionInfo.refreshCreationTimestamp();

            curResponse.setSessionInfo(curSessionInfo);
            this.sessionManager.updateSession(sessionID, curResponse);
        }
    }

    private void updateCompletedChapters(Integer chapterCount, String sessionID) {
        if (this.sessionManager != null) {
            ArchiveIngestorSession curSession = this.sessionManager.getSession(sessionID);
            ArchiveIngestorResponse curResponse = curSession.getSessionResponse();
            ArchiveIngestorSessionInfo curSessionInfo = curResponse.getSessionInfo();

            curSessionInfo.setChaptersCompleted(chapterCount);
            curSessionInfo.refreshCreationTimestamp();

            curResponse.setSessionInfo(curSessionInfo);
            this.sessionManager.updateSession(sessionID, curResponse);
        }
    }

    private void checkForCancel() throws IngestorCanceledError {
        if (Thread.interrupted()) {
            throw new IngestorCanceledError();
        }
    }

    private void handleTOSPrompt(RemoteWebDriver driver, String sessionID) throws InterruptedException {
        // Sleep for three seconds
        updateLastMessage("Waiting three seconds for possible TOS prompt...", sessionID);
        Thread.sleep(Duration.ofSeconds(TOS_SLEEP_DURATION_SECS));

        // Check for TOS specific element
        if (!driver.findElements(By.xpath("//*[@id=\"tos_agree\"]")).isEmpty()) {
            updateLastMessage("Detected TOS page. Handling contents...", sessionID);

            // Accept TOS
            driver.findElement(By.xpath("//*[@id=\"tos_agree\"]")).click();
            driver.findElement(By.xpath("//*[@id=\"data_processing_agree\"]")).click();
            driver.findElement(By.xpath("//*[@id=\"accept_tos\"]")).click();
        }
    }

    private void handleAdultContentAgreement(RemoteWebDriver driver, String sessionID) throws IngestorCanceledError {
        updateLastMessage("Checking for adult content agreement...", sessionID);
        if (!driver.findElements(By.className("caution")).isEmpty()) {
            updateLastMessage("Detected adult content agreement. Handling contents...", sessionID);

            // Hit the continue button
            driver.findElement(By.xpath("//*[@id=\"main\"]/ul/li[1]/a")).click();
        }

        // Check for cancellation of thread
        checkForCancel();
    }

    public void checkArchiveVersion(RemoteWebDriver driver, String sessionID) throws ArchiveVersionIncompatibleError {
        updateLastMessage("Checking AO3 version...", sessionID);
        WebElement archiveVersionElement = driver.findElement(By.xpath("//*[@id=\"footer\"]/ul/li[3]/ul/li[1]/a"));
        String archiveVersion = archiveVersionElement.getText();

        List<Object> compatibleArchiveVersions = this.versionTable.getJSONArray(VERSION).toList();
        if (!compatibleArchiveVersions.contains(archiveVersion)) {
            throw new ArchiveVersionIncompatibleError(archiveVersion);
        }
        else {
            updateLastMessage("AO3 version validated.", sessionID);
        }
    }

    private ArrayList<String> filterText(List<WebElement> chapterText) {
        // Create empty array list
        ArrayList<String> filteredText  = new ArrayList<>();

        for (WebElement text : chapterText) {
            // Set of tag name checks to prevent
            if (PARAGRAPH_IGNORE_TAGS.contains(text.getTagName())) {
                continue;
            }

            // Get text if the checks pass
            filteredText.add(text.getText());
        }

        // Return filtered list
        return filteredText;
    }

    private ArrayList<String> parseMetaItems(WebElement metaSection, String metaSectionClass, String sessionID) {
        updateLastMessage("Parsing " + metaSectionClass + " meta information...", sessionID);

        // Create function wide variables
        ArrayList<String> metaItems;
        List<WebElement> metaElements = metaSection.findElements(By.className(metaSectionClass));

        // Extract if not empty, otherwise send an empty list of items.
        if (!metaElements.isEmpty()) {
            WebElement metaElementLast = metaElements.getLast();
            WebElement commas = metaElementLast.findElement(By.className("commas"));

            metaItems = new ArrayList<>();
            for (WebElement item : commas.findElements(By.tagName("li"))) {
                metaItems.add(item.getText());
            }

            return metaItems;
        }
        else {
            return new ArrayList<>();
        }
    }

    private ArrayList<String> parseMetaStats(WebElement storyStats, String storyStatsClass, String sessionID) {
        updateLastMessage("Parsing " + storyStatsClass + " meta statistic...", sessionID);

        String statTitle, statValue;
        if (!storyStats.findElements(By.className(storyStatsClass)).isEmpty()) {
            statTitle = storyStats.findElements(By.className(storyStatsClass)).getFirst().getText();
            statValue = storyStats.findElements(By.className(storyStatsClass)).getLast().getText();
        }
        else {
            statTitle = PLACEHOLDER;
            statValue = PLACEHOLDER;
        }

        return new ArrayList<>(Arrays.asList(statTitle, statValue));
    }

    private StoryInfo parseStoryMetaTable(RemoteWebDriver driver, String sessionID) throws IngestorCanceledError {
        // Get the meta section
        WebElement metaSection = driver.findElement(By.className("meta"));

        // Get the rating
        ArrayList<String> storyRatingItems = parseMetaItems(metaSection, "rating", sessionID);

        // Get the warnings
        ArrayList<String> storyWarningItems = parseMetaItems(metaSection, "warning", sessionID);

        // Get the categories
        ArrayList<String> storyCategoryItems = parseMetaItems(metaSection, "category", sessionID);

        // Get the fandoms
        ArrayList<String> storyFandomItems = parseMetaItems(metaSection, "fandom", sessionID);

        // Get the relationships
        ArrayList<String> storyRelationshipItems = parseMetaItems(metaSection, "relationship", sessionID);

        // Get the characters
        ArrayList<String> storyCharacterItems = parseMetaItems(metaSection, "character", sessionID);

        // Get the freeform
        ArrayList<String> storyFreeformItems = parseMetaItems(metaSection, "freeform", sessionID);

        // Get the language
        updateLastMessage("Getting story language...", sessionID);
        String storyLanguage = metaSection.findElements(By.className("language")).getLast().getText();

        // Get the series
        updateLastMessage("Getting the series the story is a part of...", sessionID);
        List<WebElement> storySeries = metaSection.findElements(By.className("series"));
        ArrayList<String> storySeriesItems = new ArrayList<>();

        for (WebElement sS : storySeries) {
            if (!sS.findElements(By.tagName("span")).isEmpty()) {
                for (WebElement item : sS.findElements(By.xpath(DIRECT_CHILDREN_XPATH))) {
                    storySeriesItems.add(item.getText());
                }
                break;
            }
        }

        // Get the Collection
        updateLastMessage("Getting the collections the story is a part of...", sessionID);
        List<WebElement> storyCollection = metaSection.findElements(By.className("collections"));
        ArrayList<String> storyCollectionItems = new ArrayList<>();
        if (!storyCollection.isEmpty()) {
            for (WebElement item : storyCollection.getLast().findElements(By.xpath(DIRECT_CHILDREN_XPATH))) {
                storyCollectionItems.add(item.getText());
            }
        }

        // Get the stats
        WebElement storyStats = metaSection.findElements(By.className("stats")).getLast();

        String storyPublished = parseMetaStats(storyStats, "published", sessionID).getLast();
        ArrayList<String> storyStatusBundle = parseMetaStats(storyStats, "status", sessionID);
        String storyStatus = storyStatusBundle.getFirst();
        String storyStatusWhen = storyStatusBundle.getLast();
        String storyWords = parseMetaStats(storyStats, "words", sessionID).getLast();
        String storyChapters = parseMetaStats(storyStats, "chapters", sessionID).getLast();
        String storyComments = parseMetaStats(storyStats, "comments", sessionID).getLast();
        String storyKudos = parseMetaStats(storyStats, "kudos", sessionID).getLast();
        String storyBookmarks = parseMetaStats(storyStats, "bookmarks", sessionID).getLast();
        String storyHits = parseMetaStats(storyStats, "hits", sessionID).getLast();

        // Save the chapter count at instance level
        updateTotalChapters(StoryInfo.parseInitString(storyChapters), sessionID);

        // Check for cancellation of thread
        checkForCancel();

        // Return a story object
        updateLastMessage("Creating new Story instance...", sessionID);
        return new StoryInfo(
                storyRatingItems, storyWarningItems, storyCategoryItems, storyFandomItems, storyRelationshipItems,
                storyCharacterItems, storyFreeformItems, storyLanguage, storySeriesItems, storyCollectionItems,
                storyPublished, storyStatus, storyStatusWhen, storyWords, storyChapters, storyComments, storyKudos,
                storyBookmarks, storyHits
        );
    }

    private void parseStoryPrefaceInfo(WebElement workSkinSection, StoryInfo parentStoryInfo, String sessionID)
            throws IngestorCanceledError {
        // Get the preface part of the work skin
        WebElement preface = workSkinSection.findElement(By.className("preface"));

        // Get the title of the story
        updateLastMessage("Getting story title...", sessionID);
        String storyTitle = preface.findElement(By.className("title")).getText();

        // TODO: Figure out selenium-level bug where all of the authors don't show up a percentage of the time when
        //  Getting the website
        // Get the authors of the story
        updateLastMessage("Getting story authors...", sessionID);
        ArrayList<String> storyAuthors = new ArrayList<>();
        WebElement storyAuthor = preface.findElement(By.xpath(".//h3"));
        for (WebElement author : storyAuthor.findElements(By.xpath(DIRECT_CHILDREN_XPATH))) {
            storyAuthors.add(author.getText());
        }

        // Get the summary of the story if available
        updateLastMessage("Checking for story summary...", sessionID);
        List<WebElement> storySummaryList = preface.findElements(By.className("summary"));
        ArrayList<String> storySummaryText = new ArrayList<>();
        if (!storySummaryList.isEmpty()) {
            WebElement storySummaryUserStuff = storySummaryList.getFirst().findElement(By.className("userstuff"));
            storySummaryText = filterText(storySummaryUserStuff.findElements(By.xpath(ALL_CHILDREN_XPATH)));
        }

        // Get the notes of a story if available
        updateLastMessage("Checking for story notes...", sessionID);
        ArrayList<String> storyAssociationItems = new ArrayList<>();
        ArrayList<String> storyStartNoteItems = new ArrayList<>();
        ArrayList<String> storyEndNoteItems = new ArrayList<>();

        List<WebElement> storyStartNotesList = preface.findElements(By.className("notes"));
        if (!storyStartNotesList.isEmpty()) {
            updateLastMessage("Story notes found. Checking for system-made notes...", sessionID);
            if (!storyStartNotesList.getFirst().findElements(By.className("associations")).isEmpty()) {
                WebElement storyAssociation = storyStartNotesList.getFirst().findElement(By.className("associations"));
                for (WebElement association : storyAssociation.findElements(By.xpath(ALL_CHILDREN_XPATH))) {
                    storyAssociationItems.add(association.getText());
                }
            }

            updateLastMessage("Checking for user-made start notes...", sessionID);
            if (!storyStartNotesList.getFirst().findElements(By.className("userstuff")).isEmpty()) {
                WebElement storyStartNote = storyStartNotesList.getFirst().findElement(By.className("userstuff"));
                storyStartNoteItems = filterText(storyStartNote.findElements(By.xpath(ALL_CHILDREN_XPATH)));
            }
        }

        List<WebElement> storyEndNotesList = workSkinSection.findElements(By.className("afterword"));
        if (!storyEndNotesList.isEmpty()) {
            updateLastMessage("Checking for user-made end notes...", sessionID);
            if (!storyEndNotesList.getFirst().findElements(By.className("end")).isEmpty()) {
                WebElement storyEndNote = storyEndNotesList.getFirst().findElement(By.className("end"));
                WebElement storyEndNoteUserStuff = storyEndNote.findElement(By.className("userstuff"));
                storyEndNoteItems = filterText(storyEndNoteUserStuff.findElements(By.xpath(ALL_CHILDREN_XPATH)));
            }
        }

        // Update story info
        parentStoryInfo.setPrefaceInfo(
                storyTitle, storyAuthors, storySummaryText, storyAssociationItems, storyStartNoteItems,
                storyEndNoteItems
        );

        // Check for cancellation of thread
        checkForCancel();
    }

    private void parseStoryKudos(RemoteWebDriver driver, StoryInfo parentStoryInfo, String sessionID) throws IngestorCanceledError {
        WebElement kudos = driver.findElement(By.id("kudos"));
        List<WebElement> kudosClass = kudos.findElements(By.className("kudos"));

        if (!kudosClass.isEmpty()) {
            updateLastMessage("Getting list of users who gave a kudos...", sessionID);

            // Expand kudos list
            try {
                while (!kudos.findElements(By.id("kudos_more_link")).isEmpty()) {
                    kudos.findElement(By.id("kudos_more_link")).click();
                }
            }
            catch (StaleElementReferenceException | NoSuchElementException e) {
                updateLastMessage("Kudos more link detection gone wrong, continuing on...", sessionID);
            }

            // Record the text for parsing in memory
            ArrayList<String> kudosList = new ArrayList<>();
            for (WebElement user : kudosClass.getFirst().findElements(By.xpath(DIRECT_CHILDREN_XPATH))) {
                if (user.getTagName().equals("a")) {
                    kudosList.add(user.getText());
                }
            }

            parentStoryInfo.setKudosList(kudosList);
        }

        // Check for cancellation of thread
        checkForCancel();
    }

    private void parseStoryBookmarks(RemoteWebDriver driver, StoryInfo parentStoryInfo) throws IngestorCanceledError {
        // Create a wait object for later navigation
        WebDriverWait newSiteWait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_DURATION_SECS));

        WebElement metaSection = driver.findElement(By.className("meta"));
        WebElement storyStats = metaSection.findElements(By.className("stats")).getLast();

        ArrayList<String> bookmarkList = new ArrayList<>();

        List<WebElement> storyBookmarks = storyStats.findElements(By.className("bookmarks"));
        if (!storyBookmarks.isEmpty()) {
            // Click the bookmark link
            WebElement bookmarkLink = storyBookmarks.getLast();
            bookmarkLink.findElement(By.tagName("a")).click();

            // Wait for new elements to appear
            newSiteWait.until(d -> d.findElement(By.xpath("//*[@id=\"main\"]/ol")));

            // Get to the organized list and copy the users
            WebElement bookmarks = driver.findElement(By.xpath("//*[@id=\"main\"]/ol"));
            for (WebElement bookmarkBlurb : bookmarks.findElements(By.className("short"))) {
                bookmarkList.add(bookmarkBlurb.findElement(By.tagName("a")).getText());
            }

            // Go back to the last page
            driver.navigate().back();

            // Wait for certain elements to appear
            newSiteWait.until(d -> bookmarkLink.isDisplayed());
        }

        parentStoryInfo.setPublicBookmarkList(bookmarkList);

        // Check for cancellation of thread
        checkForCancel();
    }

    private Chapter parseChapterText(WebElement workSkinSection, StoryInfo parentStoryInfo, String pageTitle,
                                     String sessionID) throws ChapterContentNotFoundError, IngestorCanceledError {
        // Get the actual chapter part of the work skin
        WebElement chapter = workSkinSection.findElement(By.id("chapters"));

        // Initialize default values
        String chapterTitle = "Chapter 1: " + parentStoryInfo.title;
        ArrayList<String> chapterSummaryText = new ArrayList<>();
        ArrayList<String> chapterStartNoteText = new ArrayList<>();
        ArrayList<String> chapterEndNoteText = new ArrayList<>();
        ArrayList<String> chapterParagraphs;

        // Parse the chapter specific content
        if (!chapter.findElements(By.className("chapter")).isEmpty()) {
            updateLastMessage("Chapter class exist.", sessionID);
            WebElement chapterStuff = chapter.findElement(By.className("chapter"));

            // Get chapter title
            updateLastMessage("Getting chapter title...", sessionID);
            chapterTitle = chapterStuff.findElement(By.className("title")).getText();

            // Get chapter summary
            updateLastMessage("Checking for chapter summary...", sessionID);
            List<WebElement> chapterSummary = chapterStuff.findElements(By.className("summary"));
            if (!chapterSummary.isEmpty()) {
                WebElement chapterSummaryUserStuff = chapterSummary.getFirst().findElement(By.className("userstuff"));
                chapterSummaryText = filterText(chapterSummaryUserStuff.findElements(By.xpath(ALL_CHILDREN_XPATH)));
            }

            // Get chapter start notes
            updateLastMessage("Checking for chapter start notes...", sessionID);
            List<WebElement> chapterStartNotes = chapterStuff.findElements(By.className("notes"));
            if (!chapterStartNotes.isEmpty()) {
                WebElement chapterStartNote = chapterStartNotes.getFirst().findElement(By.className("userstuff"));
                chapterStartNoteText = filterText(chapterStartNote.findElements(By.xpath(ALL_CHILDREN_XPATH)));
            }

            // Get paragraphs from chapter user stuff
            updateLastMessage("Getting chapter paragraphs...", sessionID);
            WebElement userStuff = chapterStuff.findElement(By.className("userstuff"));
            chapterParagraphs = filterText(userStuff.findElements(By.xpath(ALL_CHILDREN_XPATH)));

            // Get chapter end notes
            updateLastMessage("Checking for chapter end notes...", sessionID);
            List<WebElement> chapterEndNotes = chapterStuff.findElements(By.className("end"));
            if (!chapterEndNotes.isEmpty()) {
                WebElement chapterEndNote = chapterEndNotes.getFirst().findElement(By.className("userstuff"));
                chapterEndNoteText = filterText(chapterEndNote.findElements(By.xpath(ALL_CHILDREN_XPATH)));
            }
        }
        else if (!chapter.findElements(By.className("userstuff")).isEmpty()) {
            // Get paragraphs from direct user stuff
            updateLastMessage("User stuff class exist. Getting user stuff paragraphs...", sessionID);
            WebElement userStuff = chapter.findElement(By.className("userstuff"));
            chapterParagraphs = filterText(userStuff.findElements(By.xpath(ALL_CHILDREN_XPATH)));
        }
        else {
            throw new ChapterContentNotFoundError();
        }

        // Check for cancellation of thread
        checkForCancel();

        // Finally return a chapter object
        return new Chapter(
                parentStoryInfo, pageTitle, chapterTitle, chapterSummaryText, chapterStartNoteText, chapterEndNoteText,
                chapterParagraphs
        );
    }

    private WebElement createCommentsPlaceholderReference(RemoteWebDriver driver) {
        return driver.findElement(By.id("comments_placeholder"));
    }

    private JSONObject parseCommentThread(WebDriverWait commentBlockWait, WebElement commentThreadElement,
                                          Integer threadDepth, String sessionID) {
        JSONObject commentThread = new JSONObject();
        String lastParentCommentID = "";

        for (WebElement commentListItem: commentThreadElement.findElements(By.xpath(DIRECT_CHILDREN_XPATH))) {
            if (!commentListItem.getTagName().equals("li")) {
                continue;
            }

            // Check what kind of list item it is
            boolean parentComment;
            String itemID = commentListItem.getAttribute("id");
            if (itemID == null) { // Null check
                parentComment = false;
            }
            else if (itemID.contains("add") || (!itemID.contains("comment") && lastParentCommentID.isBlank())) { // Skip conditions
                continue;
            }
            else if (!itemID.contains("comment")) { // False conditions
                parentComment = false;
            }
            else {
                parentComment = true;
            }

            if (parentComment) {
                try {
                    // Create new comment object
                    String commentUser = commentListItem.findElement(By.xpath(".//h4/a | .//h4/span")).getText();
                    String commentPosted = commentListItem.findElement(By.className("posted")).getText();
                    String commentID = (commentUser + '_' + commentPosted).replace(' ', '_');
                    commentID = commentID.replace(':', '_');

                    commentThread.put(commentID, new JSONObject());

                    JSONObject commentObject = commentThread.getJSONObject(commentID);
                    commentObject.put("user", commentUser);
                    commentObject.put("posted", commentPosted);

                    WebElement commentBlock = commentListItem.findElement(By.className("userstuff"));
                    ArrayList<String> commentText = filterText(commentBlock.findElements(By.xpath(ALL_CHILDREN_XPATH)));
                    commentObject.put("text", new JSONArray(commentText));

                    // Set last parent comment ID
                    lastParentCommentID = commentID;
                }
                catch (NoSuchElementException e) {
                    updateLastMessage("Skipping thread member list item...", sessionID);
                }
            }
            else {
                // Only go deeper if the max thread depth is not exceeded
                JSONObject parentCommentObject = commentThread.getJSONObject(lastParentCommentID);
                if (threadDepth <= MAX_THREAD_DEPTH) {
                    // Create new JSON array for child threads
                    try {
                        WebElement childCommentThreadElement = commentListItem.findElement(By.className("thread"));
                        parentCommentObject.put("threads", parseCommentThread(
                                commentBlockWait, childCommentThreadElement, threadDepth + 1, sessionID
                        ));
                    }
                    catch (NoSuchElementException e) {
                        updateLastMessage("Skipping thread member list item...", sessionID);
                    }
                }
                else {
                    parentCommentObject.put("threads", "Beyond max thread depth of " + MAX_THREAD_DEPTH);
                    return commentThread;
                }
            }
        }

        return commentThread;
    }

    private JSONObject parseCommentPage(RemoteWebDriver driver, String sessionID) {
        // Create the starting point
        WebElement commentsPlaceholder = createCommentsPlaceholderReference(driver);
        WebElement commentThreadElement = commentsPlaceholder.findElement(By.className("thread"));
        WebDriverWait commentBlockWait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_DURATION_SECS));

        // Start thread searching and return the result
        return parseCommentThread(commentBlockWait, commentThreadElement, 1, sessionID);
    }

    private WebElement createNextButtonReference(RemoteWebDriver driver) {
        WebElement tempPagination = driver.findElement(By.className("pagination"));
        return tempPagination.findElement(By.className("next"));
    }

    private void parseChapterComments(RemoteWebDriver driver, Chapter newChapter, String sessionID) throws IngestorCanceledError {
        // Create wait and URL checkpoint for comment navigation
        WebDriverWait commentNavWait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_DURATION_SECS));
        String currentURL = driver.getCurrentUrl();
        assert currentURL != null;

        // Open comment section
        updateLastMessage("Checking for chapter comments...", sessionID);
        List<WebElement> openCommentsButton = driver.findElements(By.id("show_comments_link"));
        boolean commentsOpened = false;
        if (!openCommentsButton.isEmpty()) {
            openCommentsButton.getFirst().click();
            commentNavWait.until(d -> openCommentsButton.getFirst().getText().contains("Hide"));
            commentsOpened = true;
        }

        // Create main comment object
        JSONObject comments = new JSONObject();
        comments.put("pages", new JSONObject());
        JSONObject commentPages = comments.getJSONObject("pages");

        // Read comment section
        if (commentsOpened) {
            updateLastMessage("Parsing chapter comments...", sessionID);

            // Check for pagination
            List<WebElement> pagination = driver.findElements(By.className("pagination"));

            if (!pagination.isEmpty()) {
                updateLastMessage("Multiple pages of comments found...", sessionID);
                boolean nextEnabled = true;
                int pageCount = 0;

                while (nextEnabled) {
                    pageCount++;
                    updateLastMessage("Parsing page number " + pageCount + " of comments...", sessionID);

                    // Get a new next button
                    WebElement next;
                    try {
                        // Wait until the comment buttons are reloaded
                        commentNavWait.until(d -> !d.findElements(By.className("next")).isEmpty());
                        next = createNextButtonReference(driver);
                    }
                    catch (NoSuchElementException | TimeoutException e) {
                        updateLastMessage("Comment page navigation failed. Skipping page...", sessionID);

                        String currentPage = driver.getCurrentUrl();
                        String[] curPagePartOne = currentPage.split("=", 2);
                        String[] curPagePartTwo = curPagePartOne[1].split("&", 2);
                        int newPageNumber = Integer.parseInt(curPagePartTwo[0]) + 1;

                        String skipPage = curPagePartOne[0] + "=" + newPageNumber + "&" + curPagePartTwo[1];
                        driver.navigate().to(skipPage);
                        continue;
                    }

                    // Parse comment page
                    commentPages.put(Integer.toString(pageCount), parseCommentPage(driver, sessionID));

                    // Check for cancellation of thread
                    checkForCancel();

                    // Check if next is still enabled
                    if (next.findElements(By.className("disabled")).isEmpty()) { // Go to next comment page
                        WebElement nextPageRef = next.findElement(By.tagName("a"));
                        String nextPage = nextPageRef.getAttribute("href");
                        assert nextPage != null;
                        driver.navigate().to(nextPage);
                    }
                    else { // Go back to original chapter page
                        driver.navigate().to(currentURL);
                        nextEnabled = false;
                    }
                }
            }
            else {
                updateLastMessage("Parsing single page of comments...", sessionID);
                commentPages.put("1", parseCommentPage(driver, sessionID));

                // Check for cancellation of thread
                checkForCancel();
            }
        }

        // Save comments to chapter
        newChapter.setComments(comments);
    }

    private Chapter parseChapter(RemoteWebDriver driver, StoryInfo parentStoryInfo, String pageTitle, String sessionID)
            throws IngestorCanceledError, ChapterContentNotFoundError {
        // Get the work skin section
        WebElement workSkinSection = driver.findElement(By.id("workskin"));

        // Parse the preface
        if (!parentStoryInfo.isSet) {
            parseStoryPrefaceInfo(workSkinSection, parentStoryInfo, sessionID);
            parseStoryKudos(driver, parentStoryInfo, sessionID);
            parseStoryBookmarks(driver, parentStoryInfo);

            // Set setting flag
            parentStoryInfo.isSet = true;
        }

        // Parse the chapter contents
        Chapter newChapter = parseChapterText(workSkinSection, parentStoryInfo, pageTitle, sessionID);
        newChapter.setPageLink(driver.getCurrentUrl());

        // Parse chapter comments
        parseChapterComments(driver, newChapter, sessionID);

        return newChapter;
    }

    public Chapter createChapter(RemoteWebDriver driver, String sessionID) throws InterruptedException,
            ChapterContentNotFoundError, IngestorCanceledError, IngestorElementNotFoundError {
        try {
            updateLastMessage("Opening website " + driver.getCurrentUrl() + "...", sessionID);

            // Check for and get past acceptance screens
            handleTOSPrompt(driver, sessionID);
            handleAdultContentAgreement(driver, sessionID);

            // Check for the correct version of otwarchive
            try {
                checkArchiveVersion(driver, sessionID);
            } catch (ArchiveVersionIncompatibleError e) {
                updateLastMessage(
                        "An archive version error was thrown. There may be some unknown behavior.", sessionID
                );
            }

            // Set the chapter completed count
            updateCompletedChapters(1, sessionID);

            // Get the title
            updateLastMessage("Getting website page title...", sessionID);
            String pageTitle = driver.getTitle();
            updateLastMessage("Parsing a chapter of " + pageTitle, sessionID);

            // Get story information
            StoryInfo newStoryInfo = parseStoryMetaTable(driver, sessionID);

            // Create a chapter object
            updateLastMessage("Creating new Chapter instance...", sessionID);
            return parseChapter(driver, newStoryInfo, pageTitle, sessionID);
        }
        catch (NoSuchElementException e) {
            throw new IngestorElementNotFoundError(driver.getCurrentUrl(), e);
        }
    }

    public Chapter createChapter(RemoteWebDriver driver, StoryInfo parentStoryInfo, String sessionID) throws InterruptedException,
            ChapterContentNotFoundError, IngestorCanceledError, IngestorElementNotFoundError {
        try {
            updateLastMessage("Opening website " + driver.getCurrentUrl() + "...", sessionID);

            // Check for and get past acceptance screens
            handleTOSPrompt(driver, sessionID);
            handleAdultContentAgreement(driver, sessionID);

            // Check for the correct version of otwarchive
            try {
                checkArchiveVersion(driver, sessionID);
            } catch (ArchiveVersionIncompatibleError e) {
                updateLastMessage(
                        "An archive version error was thrown. There may be some unknown behavior.", sessionID
                );
            }

            // Get the title
            updateLastMessage("Getting website page title...", sessionID);
            String pageTitle = driver.getTitle();

            // Create a chapter object
            updateLastMessage("Creating new Chapter instance...", sessionID);
            return parseChapter(driver, parentStoryInfo, pageTitle, sessionID);
        }
        catch (NoSuchElementException e) {
            throw new IngestorElementNotFoundError(driver.getCurrentUrl(), e);
        }
    }

    public Story createStory(RemoteWebDriver driver, String sessionID) throws InterruptedException, ChapterContentNotFoundError,
            IngestorCanceledError, IngestorElementNotFoundError {
        try {
            // Do the first chapter
            updateLastMessage("Parsing chapter 1 of " + driver.getTitle(), sessionID);

            ArrayList<Chapter> chapters = new ArrayList<>();
            chapters.add(createChapter(driver, sessionID));
            StoryInfo storyInfo = chapters.getFirst().parentStoryInfo;

            // Set the chapter completed count
            updateCompletedChapters(1, sessionID);

            // Check for cancellation of thread
            checkForCancel();

            // Do the rest of the chapters if necessary
            WebDriverWait nextChapterWait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_DURATION_SECS));
            By chapterTitleFinder = new ByChained(
                    By.id("chapters"), By.className("chapter"), By.className("preface"), By.className("title")
            );
            boolean nextButtonFound;
            int chapterCount = 2;
            do {
                // Reset flag
                nextButtonFound = false;

                // Go to next chapter if the next chapter button is found
                WebElement feedback = driver.findElement(By.id("feedback"));
                WebElement feedbackActions = feedback.findElement(By.className("actions"));
                for (WebElement action : feedbackActions.findElements(By.tagName("li"))) {
                    if (action.getText().contains("Next Chapter")) {
                        String nextChapterPage = action.findElement(By.tagName("a")).getAttribute("href");
                        assert nextChapterPage != null;
                        driver.navigate().to(nextChapterPage);
                        nextButtonFound = true; // Keeps the loop going
                        break;
                    }
                }

                // Parse next chapter
                if (nextButtonFound) {
                    nextChapterWait.until(d -> d.findElement(chapterTitleFinder).isDisplayed());
                    updateLastMessage("Parsing chapter " + chapterCount + " of " + storyInfo.title, sessionID);
                    chapters.add(createChapter(driver, storyInfo, sessionID));

                    // Set the chapter completed count
                    updateCompletedChapters(chapterCount, sessionID);

                    // Check for cancellation of thread
                    checkForCancel();

                    // Increment chapter count
                    chapterCount++;
                }
            } while (nextButtonFound);

            // Return a full story
            return new Story(storyInfo, chapters);
        }
        catch (NoSuchElementException e) {
            throw new IngestorElementNotFoundError(driver.getCurrentUrl(), e);
        }
    }
}
