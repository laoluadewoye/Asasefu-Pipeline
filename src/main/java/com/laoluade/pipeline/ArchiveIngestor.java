package com.laoluade.pipeline;

// JSON Packages
import org.json.JSONObject;

// Selenium Packages
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.WebElement;

// I/O Packages
import java.io.IOException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

// List Packages
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArchiveIngestor {
    // Class Constants
    public static final String VERSION = "0.1";
    public static final String PLACEHOLDER = "Null";

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
        Thread.sleep(3000);

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
                for (WebElement item : sS.findElements(By.xpath(".//*"))) {
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
            for (WebElement item : storyCollection.getLast().findElements(By.xpath(".//*"))) {
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
        List<WebElement> storyAuthorList = storyAuthor.findElements(By.xpath(".//*"));
        for (WebElement author : storyAuthorList) {
            storyAuthors.add(author.getText());
        }

        // Get the summary of the story if available
        System.out.println("Checking for story summary...");
        List<WebElement> storySummaryList = preface.findElements(By.className("summary"));
        ArrayList<String> storySummaryText = new ArrayList<>();
        if (!storySummaryList.isEmpty()) {
            WebElement storySummaryUserStuff = storySummaryList.getFirst().findElement(By.className("userstuff"));
            for (WebElement summaryText : storySummaryUserStuff.findElements(By.xpath(".//*"))) {
                storySummaryText.add(summaryText.getText());
            }
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
                for (WebElement association : storyAssociation.findElements(By.xpath(".//*"))) {
                    storyAssociationItems.add(association.getText());
                }
            }

            System.out.println("Checking for user-made start notes...");
            if (!storyStartNotesList.getFirst().findElements(By.className("userstuff")).isEmpty()) {
                WebElement storyStartNote = storyStartNotesList.getFirst().findElement(By.className("userstuff"));
                for (WebElement note : storyStartNote.findElements(By.xpath(".//*"))) {
                    storyStartNoteItems.add(note.getText());
                }
            }
        }

        List<WebElement> storyEndNotesList = workSkinSection.findElements(By.className("afterword"));
        if (!storyEndNotesList.isEmpty()) {
            System.out.println("Checking for user-made end notes...");
            if (!storyEndNotesList.getFirst().findElements(By.className("end")).isEmpty()) {
                WebElement storyEndNote = storyEndNotesList.getFirst().findElement(By.className("end"));
                WebElement storyEndNoteUserStuff = storyEndNote.findElement(By.className("userstuff"));
                for (WebElement note : storyEndNoteUserStuff.findElements(By.xpath(".//*"))) {
                    storyEndNoteItems.add(note.getText());
                }
            }
        }

        // Update story info
        parentStoryInfo.setPrefaceInfo(
                storyTitle, storyAuthors, storySummaryText, storyAssociationItems, storyStartNoteItems, storyEndNoteItems
        );
    }

    private Chapter parseChapterText(WebElement workSkinSection, StoryInfo parentStoryInfo, String pageTitle) {
        // Get the actual chapter part of the work skin
        WebElement chapter = workSkinSection.findElement(By.id("chapters"));

        // Initialize default values
        String chapterTitle = "Chapter 1: " + parentStoryInfo.title;
        ArrayList<String> chapterSummaryText = new ArrayList<>();
        ArrayList<String> chapterStartNoteText = new ArrayList<>();
        ArrayList<String> chapterEndNoteText = new ArrayList<>();
        ArrayList<String> chapterParagraphs = new ArrayList<>();

        // Parse the chapter specific content
        if (!chapter.findElements(By.className("chapter")).isEmpty()) {
            System.out.println("Chapter class exist.");
            WebElement chapterStuff = chapter.findElement(By.className("chapter"));

            // Get chapter title
            System.out.println("Getting chapter title...");
            chapterTitle = chapterStuff.findElement(By.className("title")).getText();

            // TODO: Add ways to ignore <strong> <em> and <u> tags
            //  Do this for notes and summaries as well
            // Get chapter summary
            System.out.println("Checking for chapter summary...");
            List<WebElement> chapterSummary = chapterStuff.findElements(By.className("summary"));
            if (!chapterSummary.isEmpty()) {
                WebElement chapterSummaryUserStuff = chapterSummary.getFirst().findElement(By.className("userstuff"));
                for (WebElement summaryText : chapterSummaryUserStuff.findElements(By.xpath(".//*"))) {
                    chapterSummaryText.add(summaryText.getText());
                }
            }

            // Get chapter start notes
            System.out.println("Checking for chapter start notes...");
            List<WebElement> chapterStartNotes = chapterStuff.findElements(By.className("notes"));
            if (!chapterStartNotes.isEmpty()) {
                WebElement chapterStartNote = chapterStartNotes.getFirst().findElement(By.className("userstuff"));
                for (WebElement note : chapterStartNote.findElements(By.xpath(".//*"))) {
                    chapterStartNoteText.add(note.getText());
                }
            }

            // Get paragraphs from chapter user stuff
            System.out.println("Getting chapter paragraphs...");
            WebElement userStuff = chapterStuff.findElement(By.className("userstuff"));
            for (WebElement paragraph : userStuff.findElements(By.xpath(".//*"))) {
                chapterParagraphs.add(paragraph.getText());
            }

            // Get chapter end notes
            System.out.println("Checking for chapter end notes...");
            List<WebElement> chapterEndNotes = chapterStuff.findElements(By.className("end"));
            if (!chapterEndNotes.isEmpty()) {
                WebElement chapterEndNote = chapterEndNotes.getFirst().findElement(By.className("userstuff"));
                for (WebElement note : chapterEndNote.findElements(By.xpath(".//*"))) {
                    chapterEndNoteText.add(note.getText());
                }
            }
        }
        else if (!chapter.findElements(By.className("userstuff")).isEmpty()) {
            // Get paragraphs from direct user stuff
            System.out.println("User stuff class exist. Getting user stuff paragraphs...");
            WebElement userStuff = chapter.findElement(By.className("userstuff"));
            for (WebElement paragraph : userStuff.findElements(By.xpath(".//*"))) {
                chapterParagraphs.add(paragraph.getText());
            }
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

    private Chapter parseChapter(RemoteWebDriver driver, StoryInfo parentStoryInfo, String pageTitle) {
        // Get the work skin section
        WebElement workSkinSection = driver.findElement(By.id("workskin"));

        // Parse the preface
        if (!parentStoryInfo.isSet) {
            parseStoryPrefaceInfo(workSkinSection, parentStoryInfo);
        }

        // Parse the chapter contents
        Chapter newChapter = parseChapterText(workSkinSection, parentStoryInfo, pageTitle);

        // TODO: Parse the chapter comments here

        return newChapter;
    }

    public Chapter createChapter(RemoteWebDriver driver, String pageLink) throws
            InterruptedException {
        // Create a driver session new page
        System.out.println("Opening website " + pageLink + "...");
        driver.get(pageLink);

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
    
    public Chapter createChapter(RemoteWebDriver driver, StoryInfo parentStoryInfo, String pageLink) throws
            InterruptedException {
        // Create a driver session new page
        System.out.println("Opening website " + pageLink + "...");
        driver.get(pageLink);

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

    public void createStory() {
        // TODO: Get bookmarks and the "left kudos" section here after creating the whole story
    }
}
