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
        List<WebElement> elements = driver.findElements(By.xpath("//*[@id=\"tos_agree\"]"));
        if (!elements.isEmpty()) {
            System.out.println("Detected TOS page. Handling contents...");

            // Accept TOS
            driver.findElement(By.xpath("//*[@id=\"tos_agree\"]")).click();
            driver.findElement(By.xpath("//*[@id=\"data_processing_agree\"]")).click();
            driver.findElement(By.xpath("//*[@id=\"accept_tos\"]")).click();
        }
    }

    private void handleAdultContentAgreement(RemoteWebDriver driver) {
        System.out.println("Checking for adult content agreement...");
        List<WebElement> elements = driver.findElements(By.className("caution"));
        if (!elements.isEmpty()) {
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
        List<WebElement> statusElements = storyStats.findElements(By.className(storyStatsClass));
        if (!statusElements.isEmpty()) {
            statTitle = storyStats.findElements(By.className(storyStatsClass)).getFirst().getText();
            statValue = storyStats.findElements(By.className(storyStatsClass)).getLast().getText();
        }
        else {
            statTitle = PLACEHOLDER;
            statValue = PLACEHOLDER;
        }

        return new ArrayList<>(Arrays.asList(statTitle, statValue));
    }

    public Chapter createChapter(RemoteWebDriver driver, String storySeries, String pageLink) throws
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

        // Get the stats
        WebElement storyStats = metaSection.findElements(By.className("stats")).getLast();

        ArrayList<String> storyStatusBundle = parseMetaStats(storyStats, "status");
        String storyStatus = storyStatusBundle.getFirst();
        String storyStatusWhen = storyStatusBundle.getLast();
        String storyPublished = parseMetaStats(storyStats, "published").getLast();
        String storyWords = parseMetaStats(storyStats, "words").getLast();
        String storyChapters = parseMetaStats(storyStats, "chapters").getLast();
        String storyComments = parseMetaStats(storyStats, "comments").getLast();
        String storyKudos = parseMetaStats(storyStats, "kudos").getLast();
        String storyBookmarks = parseMetaStats(storyStats, "bookmarks").getLast();
        String storyHits = parseMetaStats(storyStats, "hits").getLast();

        // Create a story object
        System.out.println("Creating new Story instance...");
        Story newStory = new Story(
                storySeries, storyRatingItems, storyWarningItems, storyCategoryItems, storyFandomItems,
                storyRelationshipItems, storyCharacterItems, storyFreeformItems, storyLanguage, storyPublished,
                storyStatus, storyStatusWhen, storyWords, storyChapters, storyComments, storyKudos, storyBookmarks,
                storyHits
        );
        
        // Create a chapter object
        System.out.println("Creating new Chapter instance...");
        return new Chapter(newStory, pageTitle, "1");
    }
    
    public Chapter createChapter(RemoteWebDriver driver, Story parentStory, String pageLink) throws
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
        return new Chapter(parentStory, pageTitle, "1");
    }
}
