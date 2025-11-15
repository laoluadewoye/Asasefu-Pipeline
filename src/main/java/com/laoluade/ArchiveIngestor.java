package com.laoluade;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class ArchiveIngestor {
    private final String[] casualCollectionStories = {
        "https://archiveofourown.org/works/33168574",
        "https://archiveofourown.org/works/33957277",
        "https://archiveofourown.org/works/34974553",
        "https://archiveofourown.org/works/35448004"
    };

    private final String[] casualExpansionStories = {
        "https://archiveofourown.org/works/39327156",
        "https://archiveofourown.org/works/43738372",
        "https://archiveofourown.org/works/47769598",
        "https://archiveofourown.org/works/52561213",
        "https://archiveofourown.org/works/56392429",
        "https://archiveofourown.org/works/62649829"
    };

    private final String[] asasefuStories = {"https://archiveofourown.org/works/69878601"};

    public static final String PLACEHOLDER = "Null";

    private static void handleTOSPrompt(RemoteWebDriver driver) throws InterruptedException {
        // Sleep for three seconds
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

    private static void handleAdultContentAgreement(RemoteWebDriver driver) throws InterruptedException {
        // Sleep for three seconds
        Thread.sleep(3000);

        // Check for TOS specific element
        List<WebElement> elements = driver.findElements(By.className("caution"));
        if (!elements.isEmpty()) {
            System.out.println("Detected Adult Content Agreement. Handling contents...");

            // Hit the continue button
            driver.findElement(By.xpath("//*[@id=\"main\"]/ul/li[1]/a")).click();
        }
    }

    private static ArrayList<String> parseMetaItems(WebElement metaElement) {
        WebElement commas = metaElement.findElement(By.className("commas"));

        ArrayList<String> metaItems = new ArrayList<>();
        for (WebElement item : commas.findElements(By.tagName("li"))) {
            metaItems.add(item.getText());
        }
        
        return metaItems;
    }

    public static Chapter createChapter(RemoteWebDriver driver, String storySeries, String pageLink) throws
            InterruptedException, NoSuchElementException {
        // Create a driver session new page
        driver.get(pageLink);

        // Check for and get past acceptance screens
        handleTOSPrompt(driver);
        handleAdultContentAgreement(driver);

        // Get the title
        String pageTitle = driver.getTitle();

        // Get the meta section
        WebElement metaSection = driver.findElement(By.className("meta"));

        // Get the rating
        WebElement storyRating = metaSection.findElements(By.className("rating")).getLast();
        ArrayList<String> storyRatingItems = parseMetaItems(storyRating);

        // Get the warnings
        WebElement storyWarning = metaSection.findElements(By.className("warning")).getLast();
        ArrayList<String> storyWarningItems = parseMetaItems(storyWarning);

        // Get the categories
        WebElement storyCategory = metaSection.findElements(By.className("category")).getLast();
        ArrayList<String> storyCategoryItems = parseMetaItems(storyCategory);

        // Get the fandoms
        WebElement storyFandom = metaSection.findElements(By.className("fandom")).getLast();
        ArrayList<String> storyFandomItems = parseMetaItems(storyFandom);

        // Get the freeform
        WebElement storyFreeform = metaSection.findElements(By.className("freeform")).getLast();
        ArrayList<String> storyFreeformItems = parseMetaItems(storyFreeform);

        // Get the language
        String storyLanguage = metaSection.findElements(By.className("language")).getLast().getText();

        // Get the stats
        WebElement storyStats = metaSection.findElements(By.className("stats")).getLast();

        String storyStatus, storyStatusWhen;
        List<WebElement> statusElements = storyStats.findElements(By.className("status"));
        if (!statusElements.isEmpty()) {
            storyStatus = storyStats.findElements(By.className("status")).getFirst().getText();
            storyStatusWhen = storyStats.findElements(By.className("status")).getLast().getText();
        }
        else {
            storyStatus = PLACEHOLDER;
            storyStatusWhen = PLACEHOLDER;
        }

        String storyPublished = storyStats.findElements(By.className("published")).getLast().getText();
        String storyWords = storyStats.findElements(By.className("words")).getLast().getText();
        String storyChapters = storyStats.findElements(By.className("chapters")).getLast().getText();
        String storyComments = storyStats.findElements(By.className("comments")).getLast().getText();
        String storyKudos = storyStats.findElements(By.className("kudos")).getLast().getText();
        String storyBookmarks = storyStats.findElements(By.className("bookmarks")).getLast().getText();
        String storyHits = storyStats.findElements(By.className("hits")).getLast().getText();

        // Create a story object
        Story newStory = new Story(
                storySeries, storyRatingItems, storyWarningItems, storyCategoryItems, storyFandomItems,
                storyFreeformItems, storyLanguage, storyPublished, storyStatus, storyStatusWhen, storyWords,
                storyChapters, storyComments, storyKudos, storyBookmarks, storyHits
        );
        
        // Create a chapter object
        return new Chapter(newStory, pageTitle, "1");
    }
    
    public static Chapter createChapter(RemoteWebDriver driver, Story parentStory, String pageLink) throws
            InterruptedException {
        // Create a driver session new page
        driver.get(pageLink);

        // Check for and get past acceptance screens
        handleTOSPrompt(driver);
        handleAdultContentAgreement(driver);

        // Get the title
        String pageTitle = driver.getTitle();

        // Create a chapter object
        return new Chapter(parentStory, pageTitle, "1");
    }
}
