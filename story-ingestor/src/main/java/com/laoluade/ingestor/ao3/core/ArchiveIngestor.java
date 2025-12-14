package com.laoluade.ingestor.ao3.core;

// Server Classes
import com.laoluade.ingestor.ao3.exceptions.*;
import com.laoluade.ingestor.ao3.models.ArchiveServerFutureData;
import com.laoluade.ingestor.ao3.repositories.ArchiveParse;
import com.laoluade.ingestor.ao3.repositories.ArchiveSession;
import com.laoluade.ingestor.ao3.services.ArchiveLogService;
import com.laoluade.ingestor.ao3.services.ArchiveMessageService;
import com.laoluade.ingestor.ao3.services.ArchiveSessionService;

// JSON Classes
import org.json.JSONArray;
import org.json.JSONObject;

// Selenium Classes
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.WebDriverWait;

// Spring Boot Classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// I/O Classes
import java.io.IOException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

// Structure Classes
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
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
    private final ArchiveLogService logService;

    @Autowired
    private final ArchiveMessageService messageService;

    @Autowired
    private final ArchiveSessionService sessionService;

    private final String driverSocket;

    public ArchiveIngestor(ArchiveLogService logService, ArchiveMessageService messageService,
                           ArchiveSessionService sessionService,
                           @Value("${archiveIngestor.driver.socket}") String driverSocket)
            throws IOException {
        System.out.println("Creating new Archive Ingestor...");

        System.out.println("Loading story links...");
        this.storyLinks = getJSONFromResource("story_links.json");

        System.out.println("Loading version table...");
        this.versionTable = getJSONFromResource("version_table.json");

        System.out.println("Linking to existing log service...");
        this.logService = logService;

        System.out.println("Linking to existing message service...");
        this.messageService = messageService;

        System.out.println("Linking to existing session service...");
        this.sessionService = sessionService;

        System.out.println("Setting up driver socket...");
        this.driverSocket = driverSocket;

        if (this.driverSocket != null) {
            this.logService.createInfoLog(this.messageService.createDriverSocketMessage(this.driverSocket));
        }
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

    public String getLatestOTWArchiveVersion() {
        JSONArray supportedOTWArchiveVersions = this.versionTable.getJSONArray(ArchiveIngestor.VERSION);
        return supportedOTWArchiveVersions.toList().getLast().toString();
    }

    private void updateLastRecordedMessage(String newMessage, String sessionId) {
        if (this.sessionService != null) {  // Update last message and timestamp
            this.sessionService.updateLastRecordedMessage(sessionId, newMessage);
        }
        else {  // Print the new message
            System.out.println(newMessage);
        }
    }

    private void updateTotalChapters(Integer chapterCount, String sessionId) {
        if (this.sessionService != null) {
            this.sessionService.updateChaptersTotal(sessionId, chapterCount);
        }
    }

    private void updateCompletedChapters(Integer chapterCount, String sessionId) {
        if (this.sessionService != null) {
            this.sessionService.updateChaptersCompleted(sessionId, chapterCount);
        }
    }

    private void checkForCancel(String sessionId) throws ArchiveIngestorCanceledException {
        if (this.sessionService != null) {
            if (this.sessionService.getCanceledStatus(sessionId)) {
                throw new ArchiveIngestorCanceledException();
            }
        }
    }

    private void handleTOSPrompt(RemoteWebDriver driver, String sessionId) throws InterruptedException {
        // Sleep for three seconds
        updateLastRecordedMessage("Waiting three seconds for possible TOS prompt...", sessionId);
        Thread.sleep(Duration.ofSeconds(TOS_SLEEP_DURATION_SECS));

        // Check for TOS specific element
        if (!driver.findElements(By.xpath("//*[@id=\"tos_agree\"]")).isEmpty()) {
            updateLastRecordedMessage("Detected TOS page. Handling contents...", sessionId);

            // Accept TOS
            driver.findElement(By.xpath("//*[@id=\"tos_agree\"]")).click();
            driver.findElement(By.xpath("//*[@id=\"data_processing_agree\"]")).click();
            driver.findElement(By.xpath("//*[@id=\"accept_tos\"]")).click();
        }
    }

    private void handleAdultContentAgreement(RemoteWebDriver driver, String sessionId) throws
            ArchiveIngestorCanceledException {
        updateLastRecordedMessage("Checking for adult content agreement...", sessionId);
        if (!driver.findElements(By.className("caution")).isEmpty()) {
            updateLastRecordedMessage("Detected adult content agreement. Handling contents...", sessionId);

            // Hit the continue button
            driver.findElement(By.xpath("//*[@id=\"main\"]/ul/li[1]/a")).click();
        }

        // Check for cancellation of thread
        checkForCancel(sessionId);
    }

    public void checkArchiveVersion(RemoteWebDriver driver, String sessionId) throws
            ArchiveVersionIncompatibleException {
        updateLastRecordedMessage("Checking AO3 version...", sessionId);
        WebElement archiveVersionElement = driver.findElement(By.xpath("//*[@id=\"footer\"]/ul/li[3]/ul/li[1]/a"));
        String archiveVersion = archiveVersionElement.getText();

        List<Object> compatibleArchiveVersions = this.versionTable.getJSONArray(VERSION).toList();
        if (!compatibleArchiveVersions.contains(archiveVersion)) {
            throw new ArchiveVersionIncompatibleException(archiveVersion);
        }
        else {
            updateLastRecordedMessage("AO3 version validated.", sessionId);
        }
    }

    private ArrayList<String> filterText(List<WebElement> chapterText, String sessionId) {
        // Create empty array list
        ArrayList<String> filteredText  = new ArrayList<>();

        for (WebElement text : chapterText) {
            // Set of tag name checks to prevent
            if (PARAGRAPH_IGNORE_TAGS.contains(text.getTagName())) {
                continue;
            }

            // Get text if the checks pass
            filteredText.add(text.getText());
            updateLastRecordedMessage("Added text -> " + text.getText(), sessionId);
        }

        // Return filtered list
        return filteredText;
    }

    private ArrayList<String> parseMetaItems(WebElement metaSection, String metaSectionClass, String sessionId) {
        updateLastRecordedMessage("Parsing " + metaSectionClass + " meta information...", sessionId);

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

    private ArrayList<String> parseMetaStats(WebElement storyStats, String storyStatsClass, String sessionId) {
        updateLastRecordedMessage("Parsing " + storyStatsClass + " meta statistic...", sessionId);

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

    private ArchiveStoryInfo parseStoryMetaTable(RemoteWebDriver driver, String sessionId) throws
            ArchiveIngestorCanceledException {
        // Get the meta section
        WebElement metaSection = driver.findElement(By.className("meta"));

        // Get the rating
        ArrayList<String> storyRatingItems = parseMetaItems(metaSection, "rating", sessionId);

        // Get the warnings
        ArrayList<String> storyWarningItems = parseMetaItems(metaSection, "warning", sessionId);

        // Get the categories
        ArrayList<String> storyCategoryItems = parseMetaItems(metaSection, "category", sessionId);

        // Get the fandoms
        ArrayList<String> storyFandomItems = parseMetaItems(metaSection, "fandom", sessionId);

        // Get the relationships
        ArrayList<String> storyRelationshipItems = parseMetaItems(metaSection, "relationship", sessionId);

        // Get the characters
        ArrayList<String> storyCharacterItems = parseMetaItems(metaSection, "character", sessionId);

        // Get the freeform
        ArrayList<String> storyFreeformItems = parseMetaItems(metaSection, "freeform", sessionId);

        // Get the language
        updateLastRecordedMessage("Getting story language...", sessionId);
        String storyLanguage = metaSection.findElements(By.className("language")).getLast().getText();

        // Get the series
        updateLastRecordedMessage("Getting the series the story is a part of...", sessionId);
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
        updateLastRecordedMessage("Getting the collections the story is a part of...", sessionId);
        List<WebElement> storyCollection = metaSection.findElements(By.className("collections"));
        ArrayList<String> storyCollectionItems = new ArrayList<>();
        if (!storyCollection.isEmpty()) {
            for (WebElement item : storyCollection.getLast().findElements(By.xpath(DIRECT_CHILDREN_XPATH))) {
                storyCollectionItems.add(item.getText());
            }
        }

        // Get the stats
        WebElement storyStats = metaSection.findElements(By.className("stats")).getLast();

        String storyPublished = parseMetaStats(storyStats, "published", sessionId).getLast();
        ArrayList<String> storyStatusBundle = parseMetaStats(storyStats, "status", sessionId);
        String storyStatus = storyStatusBundle.getFirst();
        String storyStatusWhen = storyStatusBundle.getLast();
        String storyWords = parseMetaStats(storyStats, "words", sessionId).getLast();
        String storyChapters = parseMetaStats(storyStats, "chapters", sessionId).getLast();
        String storyComments = parseMetaStats(storyStats, "comments", sessionId).getLast();
        String storyKudos = parseMetaStats(storyStats, "kudos", sessionId).getLast();
        String storyBookmarks = parseMetaStats(storyStats, "bookmarks", sessionId).getLast();
        String storyHits = parseMetaStats(storyStats, "hits", sessionId).getLast();

        // Check for cancellation of thread
        checkForCancel(sessionId);

        // Return a story object
        updateLastRecordedMessage("Creating new Story instance...", sessionId);
        return new ArchiveStoryInfo(
                storyRatingItems, storyWarningItems, storyCategoryItems, storyFandomItems, storyRelationshipItems,
                storyCharacterItems, storyFreeformItems, storyLanguage, storySeriesItems, storyCollectionItems,
                storyPublished, storyStatus, storyStatusWhen, storyWords, storyChapters, storyComments, storyKudos,
                storyBookmarks, storyHits
        );
    }

    private void parseStoryPrefaceInfo(WebElement workSkinSection, ArchiveStoryInfo parentArchiveStoryInfo, String sessionId)
            throws ArchiveIngestorCanceledException {
        // Get the preface part of the work skin
        WebElement preface = workSkinSection.findElement(By.className("preface"));

        // Get the title of the story
        updateLastRecordedMessage("Getting story title...", sessionId);
        String storyTitle = preface.findElement(By.className("title")).getText();

        // TODO: Figure out selenium-level bug where all of the authors don't show up a percentage of the time when
        //  Getting the website
        // Get the authors of the story
        updateLastRecordedMessage("Getting story authors...", sessionId);
        ArrayList<String> storyAuthors = new ArrayList<>();
        WebElement storyAuthor = preface.findElement(By.xpath(".//h3"));
        for (WebElement author : storyAuthor.findElements(By.xpath(DIRECT_CHILDREN_XPATH))) {
            storyAuthors.add(author.getText());
        }

        // Get the summary of the story if available
        updateLastRecordedMessage("Checking for story summary...", sessionId);
        List<WebElement> storySummaryList = preface.findElements(By.className("summary"));
        ArrayList<String> storySummaryText = new ArrayList<>();
        if (!storySummaryList.isEmpty()) {
            WebElement storySummaryUserStuff = storySummaryList.getFirst().findElement(By.className("userstuff"));
            storySummaryText = filterText(storySummaryUserStuff.findElements(By.xpath(ALL_CHILDREN_XPATH)), sessionId);
        }

        // Get the notes of a story if available
        updateLastRecordedMessage("Checking for story notes...", sessionId);
        ArrayList<String> storyAssociationItems = new ArrayList<>();
        ArrayList<String> storyStartNoteItems = new ArrayList<>();
        ArrayList<String> storyEndNoteItems = new ArrayList<>();

        List<WebElement> storyStartNotesList = preface.findElements(By.className("notes"));
        if (!storyStartNotesList.isEmpty()) {
            updateLastRecordedMessage("Story notes found. Checking for system-made notes...", sessionId);
            if (!storyStartNotesList.getFirst().findElements(By.className("associations")).isEmpty()) {
                WebElement storyAssociation = storyStartNotesList.getFirst().findElement(By.className("associations"));
                for (WebElement association : storyAssociation.findElements(By.xpath(ALL_CHILDREN_XPATH))) {
                    storyAssociationItems.add(association.getText());
                }
            }

            updateLastRecordedMessage("Checking for user-made start notes...", sessionId);
            if (!storyStartNotesList.getFirst().findElements(By.className("userstuff")).isEmpty()) {
                WebElement storyStartNote = storyStartNotesList.getFirst().findElement(By.className("userstuff"));
                storyStartNoteItems = filterText(storyStartNote.findElements(By.xpath(ALL_CHILDREN_XPATH)), sessionId);
            }
        }

        List<WebElement> storyEndNotesList = workSkinSection.findElements(By.className("afterword"));
        if (!storyEndNotesList.isEmpty()) {
            updateLastRecordedMessage("Checking for user-made end notes...", sessionId);
            if (!storyEndNotesList.getFirst().findElements(By.className("end")).isEmpty()) {
                WebElement storyEndNote = storyEndNotesList.getFirst().findElement(By.className("end"));
                WebElement storyEndNoteUserStuff = storyEndNote.findElement(By.className("userstuff"));
                storyEndNoteItems = filterText(
                        storyEndNoteUserStuff.findElements(By.xpath(ALL_CHILDREN_XPATH)), sessionId
                );
            }
        }

        // Update story info
        parentArchiveStoryInfo.setPrefaceInfo(
                storyTitle, storyAuthors, storySummaryText, storyAssociationItems, storyStartNoteItems,
                storyEndNoteItems
        );

        // Check for cancellation of thread
        checkForCancel(sessionId);
    }

    private void parseStoryKudos(RemoteWebDriver driver, ArchiveStoryInfo parentArchiveStoryInfo, String sessionId)
            throws ArchiveIngestorCanceledException {
        WebElement kudos = driver.findElement(By.id("kudos"));
        List<WebElement> kudosClass = kudos.findElements(By.className("kudos"));

        if (!kudosClass.isEmpty()) {
            updateLastRecordedMessage("Getting list of users who gave a kudos...", sessionId);

            // Expand kudos list
            try {
                while (!kudos.findElements(By.id("kudos_more_link")).isEmpty()) {
                    kudos.findElement(By.id("kudos_more_link")).click();
                }
            }
            catch (StaleElementReferenceException | NoSuchElementException e) {
                updateLastRecordedMessage("Kudos more link detection gone wrong, continuing on...", sessionId);
            }

            // Record the text for parsing in memory
            ArrayList<String> kudosList = new ArrayList<>();
            for (WebElement user : kudosClass.getFirst().findElements(By.xpath(DIRECT_CHILDREN_XPATH))) {
                if (user.getTagName().equals("a")) {
                    kudosList.add(user.getText());
                }
            }

            parentArchiveStoryInfo.setKudosList(kudosList);
        }

        // Check for cancellation of thread
        checkForCancel(sessionId);
    }

    private void parseStoryBookmarks(RemoteWebDriver driver, ArchiveStoryInfo parentArchiveStoryInfo, String sessionId)
            throws ArchiveIngestorCanceledException {
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

        parentArchiveStoryInfo.setPublicBookmarkList(bookmarkList);

        // Check for cancellation of thread
        checkForCancel(sessionId);
    }

    private ArchiveChapter parseChapterText(WebElement workSkinSection, ArchiveStoryInfo parentArchiveStoryInfo,
                                            String pageTitle, String sessionId)
            throws ArchiveParagraphsNotFoundException, ArchiveIngestorCanceledException {
        // Get the actual chapter part of the work skin
        WebElement chapter = workSkinSection.findElement(By.id("chapters"));

        // Initialize default values
        String chapterTitle = "Chapter 1: " + parentArchiveStoryInfo.title;
        ArrayList<String> chapterSummaryText = new ArrayList<>();
        ArrayList<String> chapterStartNoteText = new ArrayList<>();
        ArrayList<String> chapterEndNoteText = new ArrayList<>();
        ArrayList<String> chapterParagraphs;

        // Parse the chapter specific content
        if (!chapter.findElements(By.className("chapter")).isEmpty()) {
            updateLastRecordedMessage("Chapter class exist.", sessionId);
            WebElement chapterStuff = chapter.findElement(By.className("chapter"));

            // Get chapter title
            updateLastRecordedMessage("Getting chapter title...", sessionId);
            chapterTitle = chapterStuff.findElement(By.className("title")).getText();

            // Get chapter summary
            updateLastRecordedMessage("Checking for chapter summary...", sessionId);
            List<WebElement> chapterSummary = chapterStuff.findElements(By.className("summary"));
            if (!chapterSummary.isEmpty()) {
                WebElement chapterSummaryUserStuff = chapterSummary.getFirst().findElement(By.className("userstuff"));
                chapterSummaryText = filterText(
                        chapterSummaryUserStuff.findElements(By.xpath(ALL_CHILDREN_XPATH)), sessionId
                );
            }

            // Get chapter start notes
            updateLastRecordedMessage("Checking for chapter start notes...", sessionId);
            List<WebElement> chapterStartNotes = chapterStuff.findElements(By.className("notes"));
            if (!chapterStartNotes.isEmpty()) {
                WebElement chapterStartNote = chapterStartNotes.getFirst().findElement(By.className("userstuff"));
                chapterStartNoteText = filterText(chapterStartNote.findElements(By.xpath(ALL_CHILDREN_XPATH)), sessionId);
            }

            // Get paragraphs from chapter user stuff
            updateLastRecordedMessage("Getting chapter paragraphs...", sessionId);
            WebElement userStuff = chapterStuff.findElement(By.className("userstuff"));
            chapterParagraphs = filterText(userStuff.findElements(By.xpath(ALL_CHILDREN_XPATH)), sessionId);

            // Get chapter end notes
            updateLastRecordedMessage("Checking for chapter end notes...", sessionId);
            List<WebElement> chapterEndNotes = chapterStuff.findElements(By.className("end"));
            if (!chapterEndNotes.isEmpty()) {
                WebElement chapterEndNote = chapterEndNotes.getFirst().findElement(By.className("userstuff"));
                chapterEndNoteText = filterText(chapterEndNote.findElements(By.xpath(ALL_CHILDREN_XPATH)), sessionId);
            }
        }
        else if (!chapter.findElements(By.className("userstuff")).isEmpty()) {
            // Get paragraphs from direct user stuff
            updateLastRecordedMessage("User stuff class exist. Getting user stuff paragraphs...", sessionId);
            WebElement userStuff = chapter.findElement(By.className("userstuff"));
            chapterParagraphs = filterText(userStuff.findElements(By.xpath(ALL_CHILDREN_XPATH)), sessionId);
        }
        else {
            throw new ArchiveParagraphsNotFoundException();
        }

        // Check for cancellation of thread
        checkForCancel(sessionId);

        // Finally return a chapter object
        return new ArchiveChapter(
                parentArchiveStoryInfo, pageTitle, chapterTitle, chapterSummaryText, chapterStartNoteText,
                chapterEndNoteText, chapterParagraphs
        );
    }

    private WebElement createCommentsPlaceholderReference(RemoteWebDriver driver) {
        return driver.findElement(By.id("comments_placeholder"));
    }

    private JSONObject parseCommentThread(WebDriverWait commentBlockWait, WebElement commentThreadElement,
                                          Integer threadDepth, String sessionId) {
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
                    ArrayList<String> commentText = filterText(
                            commentBlock.findElements(By.xpath(ALL_CHILDREN_XPATH)), sessionId
                    );
                    commentObject.put("text", new JSONArray(commentText));

                    // Set last parent comment ID
                    lastParentCommentID = commentID;
                }
                catch (NoSuchElementException e) {
                    updateLastRecordedMessage("Skipping thread member list item...", sessionId);
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
                                commentBlockWait, childCommentThreadElement, threadDepth + 1, sessionId
                        ));
                    }
                    catch (NoSuchElementException e) {
                        updateLastRecordedMessage("Skipping thread member list item...", sessionId);
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

    private JSONObject parseCommentPage(RemoteWebDriver driver, String sessionId) {
        // Create the starting point
        WebElement commentsPlaceholder = createCommentsPlaceholderReference(driver);
        WebElement commentThreadElement = commentsPlaceholder.findElement(By.className("thread"));
        WebDriverWait commentBlockWait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_DURATION_SECS));

        // Start thread searching and return the result
        return parseCommentThread(commentBlockWait, commentThreadElement, 1, sessionId);
    }

    private WebElement createNextButtonReference(RemoteWebDriver driver) {
        WebElement tempPagination = driver.findElement(By.className("pagination"));
        return tempPagination.findElement(By.className("next"));
    }

    private void parseChapterComments(RemoteWebDriver driver, ArchiveChapter newArchiveChapter, String sessionId)
            throws ArchiveIngestorCanceledException {
        // Create wait and URL checkpoint for comment navigation
        WebDriverWait commentNavWait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_DURATION_SECS));
        String currentURL = driver.getCurrentUrl();
        assert currentURL != null;

        // Open comment section
        updateLastRecordedMessage("Checking for chapter comments...", sessionId);
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
            updateLastRecordedMessage("Parsing chapter comments...", sessionId);

            // Check for pagination
            List<WebElement> pagination = driver.findElements(By.className("pagination"));

            if (!pagination.isEmpty()) {
                updateLastRecordedMessage("Multiple pages of comments found...", sessionId);
                boolean nextEnabled = true;
                int pageCount = 0;

                while (nextEnabled) {
                    pageCount++;
                    updateLastRecordedMessage("Parsing page number " + pageCount + " of comments...", sessionId);

                    // Get a new next button
                    WebElement next;
                    try {
                        // Wait until the comment buttons are reloaded
                        commentNavWait.until(d -> !d.findElements(By.className("next")).isEmpty());
                        next = createNextButtonReference(driver);
                    }
                    catch (NoSuchElementException | TimeoutException e) {
                        updateLastRecordedMessage("Comment page navigation failed. Skipping page...", sessionId);

                        String currentPage = driver.getCurrentUrl();
                        String[] curPagePartOne = currentPage.split("=", 2);
                        String[] curPagePartTwo = curPagePartOne[1].split("&", 2);
                        int newPageNumber = Integer.parseInt(curPagePartTwo[0]) + 1;

                        String skipPage = curPagePartOne[0] + "=" + newPageNumber + "&" + curPagePartTwo[1];
                        driver.navigate().to(skipPage);
                        continue;
                    }

                    // Parse comment page
                    commentPages.put(Integer.toString(pageCount), parseCommentPage(driver, sessionId));

                    // Check for cancellation of thread
                    checkForCancel(sessionId);

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
                updateLastRecordedMessage("Parsing single page of comments...", sessionId);
                commentPages.put("1", parseCommentPage(driver, sessionId));

                // Check for cancellation of thread
                checkForCancel(sessionId);
            }
        }

        // Save comments to chapter
        newArchiveChapter.setComments(comments);
    }

    private ArchiveChapter parseChapter(RemoteWebDriver driver, ArchiveStoryInfo parentArchiveStoryInfo,
                                        String pageTitle, String sessionId)
            throws ArchiveIngestorCanceledException, ArchiveParagraphsNotFoundException {
        // Get the work skin section
        WebElement workSkinSection = driver.findElement(By.id("workskin"));

        // Parse the preface
        if (!parentArchiveStoryInfo.isSet) {
            parseStoryPrefaceInfo(workSkinSection, parentArchiveStoryInfo, sessionId);
            parseStoryKudos(driver, parentArchiveStoryInfo, sessionId);
            parseStoryBookmarks(driver, parentArchiveStoryInfo, sessionId);

            // Set setting flag
            parentArchiveStoryInfo.isSet = true;
        }

        // Parse the chapter contents
        ArchiveChapter newArchiveChapter = parseChapterText(workSkinSection, parentArchiveStoryInfo, pageTitle, sessionId);
        updateLastRecordedMessage("Navigating back to current URL...", sessionId);
        newArchiveChapter.setPageLink(driver.getCurrentUrl());

        // Parse chapter comments
        parseChapterComments(driver, newArchiveChapter, sessionId);

        return newArchiveChapter;
    }

    public ArchiveChapter createChapter(RemoteWebDriver driver, String sessionId) throws InterruptedException,
            ArchiveParagraphsNotFoundException, ArchiveIngestorCanceledException, ArchiveElementNotFoundException,
            ArchivePageNotFoundException {
        try {
            updateLastRecordedMessage("Opening website " + driver.getCurrentUrl() + "...", sessionId);

            // Check for and get past acceptance screens
            handleTOSPrompt(driver, sessionId);
            handleAdultContentAgreement(driver, sessionId);

            // Check for the correct version of otwarchive
            try {
                checkArchiveVersion(driver, sessionId);
            } catch (ArchiveVersionIncompatibleException e) {
                updateLastRecordedMessage(
                        "An archive version error was thrown. There may be some unknown behavior.", sessionId
                );
            }

            // Get the title
            updateLastRecordedMessage("Getting website page title...", sessionId);
            String pageTitle = driver.getTitle();
            if (pageTitle == null) {
                throw new ArchivePageNotFoundException();
            }
            if (this.messageService != null) {
                if (pageTitle.equals(this.messageService.getArchiveNotFoundPageTitle())) {
                    updateLastRecordedMessage(this.messageService.getLoggingErrorParseFailedNotFound(), sessionId);
                    throw new ArchivePageNotFoundException();
                }
            }
            updateLastRecordedMessage("Parsing a chapter of " + pageTitle, sessionId);

            // Get story information
            ArchiveStoryInfo newArchiveStoryInfo = parseStoryMetaTable(driver, sessionId);

            // Save total chapter count
            updateTotalChapters(newArchiveStoryInfo.totalChapters, sessionId);

            // Set the chapter completed count
            updateCompletedChapters(1, sessionId);

            // Create a chapter object
            updateLastRecordedMessage("Creating new Chapter instance...", sessionId);
            return parseChapter(driver, newArchiveStoryInfo, pageTitle, sessionId);
        }
        catch (NoSuchElementException e) {
            throw new ArchiveElementNotFoundException(driver.getCurrentUrl(), e);
        }
    }

    public ArchiveChapter createChapter(RemoteWebDriver driver, ArchiveStoryInfo parentArchiveStoryInfo,
                                        String sessionId) throws InterruptedException,
            ArchiveParagraphsNotFoundException, ArchiveIngestorCanceledException, ArchiveElementNotFoundException {
        try {
            updateLastRecordedMessage("Opening website " + driver.getCurrentUrl() + "...", sessionId);

            // Check for and get past acceptance screens
            handleTOSPrompt(driver, sessionId);
            handleAdultContentAgreement(driver, sessionId);

            // Check for the correct version of otwarchive
            try {
                checkArchiveVersion(driver, sessionId);
            } catch (ArchiveVersionIncompatibleException e) {
                updateLastRecordedMessage(
                        "An archive version error was thrown. There may be some unknown behavior.", sessionId
                );
            }

            // Get the title
            updateLastRecordedMessage("Getting website page title...", sessionId);
            String pageTitle = driver.getTitle();

            // Create a chapter object
            updateLastRecordedMessage("Creating new Chapter instance...", sessionId);
            return parseChapter(driver, parentArchiveStoryInfo, pageTitle, sessionId);
        }
        catch (NoSuchElementException e) {
            throw new ArchiveElementNotFoundException(driver.getCurrentUrl(), e);
        }
    }

    public ArchiveStory createStory(RemoteWebDriver driver, String sessionId) throws InterruptedException,
            ArchiveParagraphsNotFoundException, ArchiveIngestorCanceledException, ArchiveElementNotFoundException,
            ArchivePageNotFoundException {
        try {
            // Do the first chapter
            updateLastRecordedMessage("Parsing chapter 1 of " + driver.getTitle(), sessionId);

            ArrayList<ArchiveChapter> archiveChapters = new ArrayList<>();
            archiveChapters.add(createChapter(driver, sessionId));
            ArchiveStoryInfo archiveStoryInfo = archiveChapters.getFirst().parentArchiveStoryInfo;

            // Set the chapter completed count
            updateCompletedChapters(1, sessionId);

            // Check for cancellation of thread
            checkForCancel(sessionId);

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
                    updateLastRecordedMessage("Parsing chapter " + chapterCount + " of " + archiveStoryInfo.title, sessionId);
                    archiveChapters.add(createChapter(driver, archiveStoryInfo, sessionId));

                    // Set the chapter completed count
                    updateCompletedChapters(chapterCount, sessionId);

                    // Check for cancellation of thread
                    checkForCancel(sessionId);

                    // Increment chapter count
                    chapterCount++;
                }
            } while (nextButtonFound);

            // Return a full story
            return new ArchiveStory(archiveStoryInfo, archiveChapters);
        }
        catch (NoSuchElementException e) {
            throw new ArchiveElementNotFoundException(driver.getCurrentUrl(), e);
        }
    }

    public CompletableFuture<ArchiveServerFutureData> returnFailedFuture(String resultMessage, String sessionId) {
        // Log error
        this.logService.createErrorLog(resultMessage);

        // Set flag and timestamp on session entity
        ArchiveSession curSessionEntity = this.sessionService.getSession(sessionId);
        if (resultMessage.equals(this.messageService.getLoggingErrorParseFailedCanceled())) {
            curSessionEntity.setSessionCanceled(true);
        }
        else {
            curSessionEntity.setSessionException(true);
        }
        curSessionEntity.setSessionUpdated(this.messageService.getNowTimestampString());

        // Update the last message
        this.sessionService.updateLastRecordedMessage(sessionId, resultMessage);

        // Update the session entity
        this.sessionService.updateSessionFull(sessionId, curSessionEntity);

        // Return failed future
        return CompletableFuture.completedFuture(new ArchiveServerFutureData(resultMessage, false));
    }

    public CompletableFuture<ArchiveServerFutureData> returnCompletedFuture(String newJSONString, String resultMessage,
                                                                            String sessionId) {
        // Log info
        this.logService.createInfoLog(resultMessage);

        // Update entities
        ArchiveSession curSessionEntity = this.sessionService.getSession(sessionId);
        ArchiveParse curParseEntity = curSessionEntity.getParseEntity();

        curParseEntity.setParseResult(newJSONString);

        curSessionEntity.setParseEntity(curParseEntity);
        curSessionEntity.setSessionFinished(true);
        curSessionEntity.setSessionUpdated(this.messageService.getNowTimestampString());

        // Update the last message
        this.sessionService.updateLastRecordedMessage(sessionId, resultMessage);

        // Save the updated session entity
        this.sessionService.updateSessionFull(sessionId, curSessionEntity);

        // Return the completed future
        return CompletableFuture.completedFuture(new ArchiveServerFutureData(resultMessage, true));
    }

    // TODO: Reset any possible session and navigate to the chapter URL
    public RemoteWebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        URL containerLocator;

        try {
            URI containerIdentifier = new URI(this.driverSocket);
            containerLocator = containerIdentifier.toURL();
        }
        catch (URISyntaxException | MalformedURLException e) {
            this.logService.createInfoLog(this.messageService.createChapterURLExceptionMessage(this.driverSocket));
            return null;
        }

        RemoteWebDriver driver = new RemoteWebDriver(containerLocator, options);
        this.logService.createInfoLog(this.messageService.getLoggingInfoChapterCreatedDriver());

        if (driver.getSessionId() != null) {
            this.logService.createInfoLog(this.messageService.getLoggingInfoActiveSessionFound());
            driver.quit();
            driver = new RemoteWebDriver(containerLocator, options);
        }
        else {
            this.logService.createInfoLog(this.messageService.getLoggingInfoNoActiveSessionFound());
        }

        return driver;
    }

    @Async("archiveIngestorAsyncExecutor")
    public CompletableFuture<ArchiveServerFutureData> startCreateChapterTask(String chapterLink, String sessionId) {
        // Create a driver and chapter objects
        RemoteWebDriver driver = this.createDriver();
        ArchiveChapter newArchiveChapter;

        // Try to parse and quit the driver after the attempt
        try {
            driver.get(chapterLink);
            newArchiveChapter = this.createChapter(driver, sessionId);
            this.logService.createInfoLog(this.messageService.getLoggingInfoChapterParseSucceeded());
            driver.quit();
            this.logService.createInfoLog(this.messageService.getLoggingInfoChapterQuitDriver());
        }
        catch (InterruptedException e) {
            driver.quit();
            this.logService.createInfoLog(this.messageService.getLoggingInfoChapterQuitDriver());
            return returnFailedFuture(this.messageService.getLoggingErrorChapterFailedInterrupt(), sessionId);
        }
        catch (ArchiveParagraphsNotFoundException e) {
            driver.quit();
            this.logService.createInfoLog(this.messageService.getLoggingInfoChapterQuitDriver());
            return returnFailedFuture(this.messageService.getLoggingErrorChapterFailedContent(), sessionId);
        }
        catch (ArchiveElementNotFoundException e) {
            driver.quit();
            this.logService.createInfoLog(this.messageService.getLoggingInfoChapterQuitDriver());
            return returnFailedFuture(this.messageService.getLoggingErrorParseFailedElement(), sessionId);
        }
        catch (ArchiveIngestorCanceledException e) {
            driver.quit();
            this.logService.createInfoLog(this.messageService.getLoggingInfoChapterQuitDriver());
            return returnFailedFuture(this.messageService.getLoggingErrorParseFailedCanceled(), sessionId);
        }
        catch (ArchivePageNotFoundException e) {
            driver.quit();
            this.logService.createInfoLog(this.messageService.getLoggingInfoChapterQuitDriver());
            return returnFailedFuture(this.messageService.getLoggingErrorParseFailedNotFound(), sessionId);
        }

        // Set the response instance with the chapter's contents and return everything
        String newChapterJSONString = newArchiveChapter.getJSONRepWithParent().toString();
        return returnCompletedFuture(
                newChapterJSONString, this.messageService.getLoggingInfoChapterRetrievedJSON(), sessionId
        );
    }

    @Async("archiveIngestorAsyncExecutor")
    public CompletableFuture<ArchiveServerFutureData> startCreateStoryTask(String storyLink, String sessionId) {
        // Create a driver and story objects
        RemoteWebDriver driver = this.createDriver();
        ArchiveStory newArchiveStory;

        try {
            driver.get(storyLink);
            newArchiveStory = this.createStory(driver, sessionId);
            this.logService.createInfoLog(this.messageService.getLoggingInfoStoryParseSucceeded());
            driver.quit();
            this.logService.createInfoLog(this.messageService.getLoggingInfoStoryQuitDriver());
        }
        catch (InterruptedException e) {
            driver.quit();
            this.logService.createInfoLog(this.messageService.getLoggingInfoStoryQuitDriver());
            return returnFailedFuture(this.messageService.getLoggingErrorStoryFailedInterrupt(), sessionId);
        }
        catch (ArchiveParagraphsNotFoundException e) {
            driver.quit();
            this.logService.createInfoLog(this.messageService.getLoggingInfoStoryQuitDriver());
            return returnFailedFuture(this.messageService.getLoggingErrorStoryFailedContent(), sessionId);
        }
        catch (ArchiveElementNotFoundException e) {
            driver.quit();
            this.logService.createInfoLog(this.messageService.getLoggingInfoStoryQuitDriver());
            return returnFailedFuture(this.messageService.getLoggingErrorParseFailedElement(), sessionId);
        }
        catch (ArchiveIngestorCanceledException e) {
            driver.quit();
            this.logService.createInfoLog(this.messageService.getLoggingInfoStoryQuitDriver());
            return returnFailedFuture(this.messageService.getLoggingErrorParseFailedCanceled(), sessionId);
        }
        catch (ArchivePageNotFoundException e) {
            driver.quit();
            this.logService.createInfoLog(this.messageService.getLoggingInfoChapterQuitDriver());
            return returnFailedFuture(this.messageService.getLoggingErrorParseFailedNotFound(), sessionId);
        }

        // Set the response instance with the story's contents and return everything
        String newStoryJSONString = newArchiveStory.getJSONRep().toString();
        return returnCompletedFuture(
                newStoryJSONString, this.messageService.getLoggingInfoStoryRetrievedJSON(), sessionId
        );
    }
}
