package com.laoluade.ao3;

// JSON Packages
import org.json.JSONArray;
import org.json.JSONObject;

// Selenium Packages
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.WebDriverWait;

// I/O Packages
import java.io.IOException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

// Structure Packages
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.Duration;

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

    public ArchiveIngestor() throws IOException {
        System.out.println("Creating new Archive Ingestor...");

        System.out.println("Loading story links...");
        storyLinks = getJSONFromResource("story_links.json");

        System.out.println("Loading version table...");
        versionTable = getJSONFromResource("version_table.json");
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

    private void handleTOSPrompt(RemoteWebDriver driver) throws InterruptedException {
        // Sleep for three seconds
        System.out.println("Waiting three seconds for possible TOS prompt...");
        Thread.sleep(Duration.ofSeconds(TOS_SLEEP_DURATION_SECS));

        // Check for TOS specific element
        if (!driver.findElements(By.xpath("//*[@id=\"tos_agree\"]")).isEmpty()) {
            System.out.println("Detected TOS page. Handling contents...");

            // Accept TOS
            driver.findElement(By.xpath("//*[@id=\"tos_agree\"]")).click();
            driver.findElement(By.xpath("//*[@id=\"data_processing_agree\"]")).click();
            driver.findElement(By.xpath("//*[@id=\"accept_tos\"]")).click();
        }
    }

    private void handleAdultContentAgreement(RemoteWebDriver driver) {
        System.out.println("Checking for adult content agreement...");
        if (!driver.findElements(By.className("caution")).isEmpty()) {
            System.out.println("Detected adult content agreement. Handling contents...");

            // Hit the continue button
            driver.findElement(By.xpath("//*[@id=\"main\"]/ul/li[1]/a")).click();
        }
    }

    private void checkArchiveVersion(RemoteWebDriver driver) {
        System.out.println("Checking AO3 version...");
        WebElement archiveVersionElement = driver.findElement(By.xpath("//*[@id=\"footer\"]/ul/li[3]/ul/li[1]/a"));
        String archiveVersion = archiveVersionElement.getText();

        List<Object> compatibleArchiveVersions = this.versionTable.getJSONArray(VERSION).toList();
        if (!compatibleArchiveVersions.contains(archiveVersion)) {
            throw new ArchiveVersionIncompatibleError(archiveVersion);
        }
        else {
            System.out.println("AO3 version validated.");
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

    private ArrayList<String> parseMetaItems(WebElement metaSection, String metaSectionClass) {
        System.out.println("Parsing " + metaSectionClass + " meta information...");

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

    private ArrayList<String> parseMetaStats(WebElement storyStats, String storyStatsClass) {
        System.out.println("Parsing " + storyStatsClass + " meta statistic...");

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

    private StoryInfo parseStoryMetaTable(RemoteWebDriver driver) throws InterruptedException {
        // Get the meta section
        WebElement metaSection = driver.findElement(By.className("meta"));

        // Get the rating
        ArrayList<String> storyRatingItems = parseMetaItems(metaSection, "rating");

        // Get the warnings
        ArrayList<String> storyWarningItems = parseMetaItems(metaSection, "warning");

        // Get the categories
        ArrayList<String> storyCategoryItems = parseMetaItems(metaSection, "category");

        // Get the fandoms
        ArrayList<String> storyFandomItems = parseMetaItems(metaSection, "fandom");

        // Get the relationships
        ArrayList<String> storyRelationshipItems = parseMetaItems(metaSection, "relationship");

        // Get the characters
        ArrayList<String> storyCharacterItems = parseMetaItems(metaSection, "character");

        // Get the freeform
        ArrayList<String> storyFreeformItems = parseMetaItems(metaSection, "freeform");

        // Get the language
        System.out.println("Getting story language...");
        String storyLanguage = metaSection.findElements(By.className("language")).getLast().getText();

        // Get the series
        System.out.println("Getting the series the story is a part of...");
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
        System.out.println("Getting the collections the story is a part of...");
        List<WebElement> storyCollection = metaSection.findElements(By.className("collections"));
        ArrayList<String> storyCollectionItems = new ArrayList<>();
        if (!storyCollection.isEmpty()) {
            for (WebElement item : storyCollection.getLast().findElements(By.xpath(DIRECT_CHILDREN_XPATH))) {
                storyCollectionItems.add(item.getText());
            }
        }

        // Get the stats
        WebElement storyStats = metaSection.findElements(By.className("stats")).getLast();

        String storyPublished = parseMetaStats(storyStats, "published").getLast();
        ArrayList<String> storyStatusBundle = parseMetaStats(storyStats, "status");
        String storyStatus = storyStatusBundle.getFirst();
        String storyStatusWhen = storyStatusBundle.getLast();
        String storyWords = parseMetaStats(storyStats, "words").getLast();
        String storyChapters = parseMetaStats(storyStats, "chapters").getLast();
        String storyComments = parseMetaStats(storyStats, "comments").getLast();
        String storyKudos = parseMetaStats(storyStats, "kudos").getLast();
        String storyBookmarks = parseMetaStats(storyStats, "bookmarks").getLast();
        String storyHits = parseMetaStats(storyStats, "hits").getLast();

        // Return a story object
        System.out.println("Creating new Story instance...");
        return new StoryInfo(
                storyRatingItems, storyWarningItems, storyCategoryItems, storyFandomItems, storyRelationshipItems,
                storyCharacterItems, storyFreeformItems, storyLanguage, storySeriesItems, storyCollectionItems,
                storyPublished, storyStatus, storyStatusWhen, storyWords, storyChapters, storyComments, storyKudos,
                storyBookmarks, storyHits
        );
    }

    private void parseStoryPrefaceInfo(WebElement workSkinSection, StoryInfo parentStoryInfo) {
        // Get the preface part of the work skin
        WebElement preface = workSkinSection.findElement(By.className("preface"));

        // Get the title of the story
        System.out.println("Getting story title...");
        String storyTitle = preface.findElement(By.className("title")).getText();

        // TODO: Figure out selenium-level bug where all of the authors don't show up a percentage of the time when
        //  Getting the website
        // Get the authors of the story
        System.out.println("Getting story authors...");
        ArrayList<String> storyAuthors = new ArrayList<>();
        WebElement storyAuthor = preface.findElement(By.xpath(".//h3"));
        for (WebElement author : storyAuthor.findElements(By.xpath(DIRECT_CHILDREN_XPATH))) {
            storyAuthors.add(author.getText());
        }

        // Get the summary of the story if available
        System.out.println("Checking for story summary...");
        List<WebElement> storySummaryList = preface.findElements(By.className("summary"));
        ArrayList<String> storySummaryText = new ArrayList<>();
        if (!storySummaryList.isEmpty()) {
            WebElement storySummaryUserStuff = storySummaryList.getFirst().findElement(By.className("userstuff"));
            storySummaryText = filterText(storySummaryUserStuff.findElements(By.xpath(ALL_CHILDREN_XPATH)));
        }

        // Get the notes of a story if available
        System.out.println("Checking for story notes...");
        ArrayList<String> storyAssociationItems = new ArrayList<>();
        ArrayList<String> storyStartNoteItems = new ArrayList<>();
        ArrayList<String> storyEndNoteItems = new ArrayList<>();

        List<WebElement> storyStartNotesList = preface.findElements(By.className("notes"));
        if (!storyStartNotesList.isEmpty()) {
            System.out.println("Story notes found. Checking for system-made notes...");
            if (!storyStartNotesList.getFirst().findElements(By.className("associations")).isEmpty()) {
                WebElement storyAssociation = storyStartNotesList.getFirst().findElement(By.className("associations"));
                for (WebElement association : storyAssociation.findElements(By.xpath(ALL_CHILDREN_XPATH))) {
                    storyAssociationItems.add(association.getText());
                }
            }

            System.out.println("Checking for user-made start notes...");
            if (!storyStartNotesList.getFirst().findElements(By.className("userstuff")).isEmpty()) {
                WebElement storyStartNote = storyStartNotesList.getFirst().findElement(By.className("userstuff"));
                storyStartNoteItems = filterText(storyStartNote.findElements(By.xpath(ALL_CHILDREN_XPATH)));
            }
        }

        List<WebElement> storyEndNotesList = workSkinSection.findElements(By.className("afterword"));
        if (!storyEndNotesList.isEmpty()) {
            System.out.println("Checking for user-made end notes...");
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
    }

    private void parseStoryKudos(RemoteWebDriver driver, StoryInfo parentStoryInfo) {
        WebElement kudos = driver.findElement(By.id("kudos"));
        List<WebElement> kudosClass = kudos.findElements(By.className("kudos"));

        if (!kudosClass.isEmpty()) {
            System.out.println("Getting list of users who gave a kudos...");

            // Expand kudos list
            try {
                while (!kudos.findElements(By.id("kudos_more_link")).isEmpty()) {
                    kudos.findElement(By.id("kudos_more_link")).click();
                }
            }
            catch (StaleElementReferenceException | NoSuchElementException e) {
                System.out.println("Kudos more link detection gone wrong, continuing on...");
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
    }

    private void parseStoryBookmarks(RemoteWebDriver driver, StoryInfo parentStoryInfo) {
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
    }

    private Chapter parseChapterText(WebElement workSkinSection, StoryInfo parentStoryInfo, String pageTitle) {
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
            System.out.println("Chapter class exist.");
            WebElement chapterStuff = chapter.findElement(By.className("chapter"));

            // Get chapter title
            System.out.println("Getting chapter title...");
            chapterTitle = chapterStuff.findElement(By.className("title")).getText();

            // Get chapter summary
            System.out.println("Checking for chapter summary...");
            List<WebElement> chapterSummary = chapterStuff.findElements(By.className("summary"));
            if (!chapterSummary.isEmpty()) {
                WebElement chapterSummaryUserStuff = chapterSummary.getFirst().findElement(By.className("userstuff"));
                chapterSummaryText = filterText(chapterSummaryUserStuff.findElements(By.xpath(ALL_CHILDREN_XPATH)));
            }

            // Get chapter start notes
            System.out.println("Checking for chapter start notes...");
            List<WebElement> chapterStartNotes = chapterStuff.findElements(By.className("notes"));
            if (!chapterStartNotes.isEmpty()) {
                WebElement chapterStartNote = chapterStartNotes.getFirst().findElement(By.className("userstuff"));
                chapterStartNoteText = filterText(chapterStartNote.findElements(By.xpath(ALL_CHILDREN_XPATH)));
            }

            // Get paragraphs from chapter user stuff
            System.out.println("Getting chapter paragraphs...");
            WebElement userStuff = chapterStuff.findElement(By.className("userstuff"));
            chapterParagraphs = filterText(userStuff.findElements(By.xpath(ALL_CHILDREN_XPATH)));

            // Get chapter end notes
            System.out.println("Checking for chapter end notes...");
            List<WebElement> chapterEndNotes = chapterStuff.findElements(By.className("end"));
            if (!chapterEndNotes.isEmpty()) {
                WebElement chapterEndNote = chapterEndNotes.getFirst().findElement(By.className("userstuff"));
                chapterEndNoteText = filterText(chapterEndNote.findElements(By.xpath(ALL_CHILDREN_XPATH)));
            }
        }
        else if (!chapter.findElements(By.className("userstuff")).isEmpty()) {
            // Get paragraphs from direct user stuff
            System.out.println("User stuff class exist. Getting user stuff paragraphs...");
            WebElement userStuff = chapter.findElement(By.className("userstuff"));
            chapterParagraphs = filterText(userStuff.findElements(By.xpath(ALL_CHILDREN_XPATH)));
        }
        else {
            throw new ChapterContentNotFoundError();
        }

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
                                          Integer threadDepth) {
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
                    System.out.println("Skipping thread member list item...");
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
                                commentBlockWait, childCommentThreadElement, threadDepth + 1)
                        );
                    }
                    catch (NoSuchElementException e) {
                        System.out.println("Skipping thread member list item...");
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

    private JSONObject parseCommentPage(RemoteWebDriver driver) {
        // Create the starting point
        WebElement commentsPlaceholder = createCommentsPlaceholderReference(driver);
        WebElement commentThreadElement = commentsPlaceholder.findElement(By.className("thread"));
        WebDriverWait commentBlockWait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_DURATION_SECS));

        // Start thread searching and return the result
        return parseCommentThread(commentBlockWait, commentThreadElement, 1);
    }

    private WebElement createNextButtonReference(RemoteWebDriver driver) {
        WebElement tempPagination = driver.findElement(By.className("pagination"));
        return tempPagination.findElement(By.className("next"));
    }

    private void parseChapterComments(RemoteWebDriver driver, Chapter newChapter) {
        // Create wait and URL checkpoint for comment navigation
        WebDriverWait commentNavWait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_DURATION_SECS));
        String currentURL = driver.getCurrentUrl();
        assert currentURL != null;

        // Open comment section
        System.out.println("Checking for chapter comments...");
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
            System.out.println("Parsing chapter comments...");

            // Check for pagination
            List<WebElement> pagination = driver.findElements(By.className("pagination"));

            if (!pagination.isEmpty()) {
                System.out.println("Multiple pages of comments found...");
                boolean nextEnabled = true;
                int pageCount = 0;

                while (nextEnabled) {
                    pageCount++;
                    System.out.println("Parsing page number " + pageCount + " of comments...");

                    // Get a new next button
                    WebElement next;
                    try {
                        // Wait until the comment buttons are reloaded
                        commentNavWait.until(d -> !d.findElements(By.className("next")).isEmpty());
                        next = createNextButtonReference(driver);
                    }
                    catch (NoSuchElementException | TimeoutException e) {
                        System.out.println("Comment page navigation failed. Skipping page...");

                        String currentPage = driver.getCurrentUrl();
                        String[] curPagePartOne = currentPage.split("=", 2);
                        String[] curPagePartTwo = curPagePartOne[1].split("&", 2);
                        int newPageNumber = Integer.parseInt(curPagePartTwo[0]) + 1;

                        String skipPage = curPagePartOne[0] + "=" + newPageNumber + "&" + curPagePartTwo[1];
                        driver.navigate().to(skipPage);
                        continue;
                    }

                    // Parse comment page
                    commentPages.put(Integer.toString(pageCount), parseCommentPage(driver));

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
                System.out.println("Parsing single page of comments...");
                commentPages.put("1", parseCommentPage(driver));
            }
        }

        // Save comments to chapter
        newChapter.setComments(comments);
    }

    private Chapter parseChapter(RemoteWebDriver driver, StoryInfo parentStoryInfo, String pageTitle) throws InterruptedException {
        // Get the work skin section
        WebElement workSkinSection = driver.findElement(By.id("workskin"));

        // Parse the preface
        if (!parentStoryInfo.isSet) {
            parseStoryPrefaceInfo(workSkinSection, parentStoryInfo);
            parseStoryKudos(driver, parentStoryInfo);
            parseStoryBookmarks(driver, parentStoryInfo);

            // Set setting flag
            parentStoryInfo.isSet = true;
        }

        // Parse the chapter contents
        Chapter newChapter = parseChapterText(workSkinSection, parentStoryInfo, pageTitle);
        newChapter.setPageLink(driver.getCurrentUrl());

        // Parse chapter comments
        parseChapterComments(driver, newChapter);

        return newChapter;
    }

    public Chapter createChapter(RemoteWebDriver driver) throws InterruptedException {
        // Check for and get past acceptance screens
        handleTOSPrompt(driver);
        handleAdultContentAgreement(driver);

        // Check for the correct version of otwarchive
        checkArchiveVersion(driver);

        // Get the title
        System.out.println("Getting website page title...");
        String pageTitle = driver.getTitle();

        // Get story information
        StoryInfo newStoryInfo = parseStoryMetaTable(driver);
        
        // Create a chapter object
        System.out.println("Creating new Chapter instance...");
        return parseChapter(driver, newStoryInfo, pageTitle);
    }

    public Chapter createChapter(RemoteWebDriver driver, StoryInfo parentStoryInfo) throws InterruptedException {
        // Check for and get past acceptance screens
        handleTOSPrompt(driver);
        handleAdultContentAgreement(driver);

        // Check for the correct version of otwarchive
        checkArchiveVersion(driver);

        // Get the title
        System.out.println("Getting website page title...");
        String pageTitle = driver.getTitle();

        // Create a chapter object
        System.out.println("Creating new Chapter instance...");
        return parseChapter(driver, parentStoryInfo, pageTitle);
    }

    // TODO: Create way to parse through story, then maybe even a series
    public Story createStory(RemoteWebDriver driver) throws InterruptedException {
        // Do the first chapter
        System.out.println("Parsing chapter 1 of " + driver.getTitle());

        ArrayList<Chapter> chapters = new ArrayList<>();
        chapters.add(createChapter(driver));
        StoryInfo storyInfo = chapters.getFirst().parentStoryInfo;

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
                System.out.println("Parsing chapter " + chapterCount + " of " + storyInfo.title);
                chapters.add(createChapter(driver, storyInfo));
                chapterCount++;
            }
        } while (nextButtonFound);

        // Return a full story
        return new Story(storyInfo, chapters);
    }
}
