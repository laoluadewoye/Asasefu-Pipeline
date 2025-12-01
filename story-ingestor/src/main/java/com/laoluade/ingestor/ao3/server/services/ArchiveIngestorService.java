package com.laoluade.ingestor.ao3.server.services;

// Local classes
import com.laoluade.ingestor.ao3.core.ArchiveIngestor;
import com.laoluade.ingestor.ao3.core.Chapter;
import com.laoluade.ingestor.ao3.core.Story;
import com.laoluade.ingestor.ao3.core.StoryInfo;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorInfo;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorRequest;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorResponse;

// Third party classes
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Java classes
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

// TODO: Add logging using org.slf4j.Logger and org.slf4j.LoggerFactory
@Service
public class ArchiveIngestorService {
    // Attributes
    private final ArchiveIngestor archiveIngestor;
    private final String driverSocket;

    // Constants
    private final String EMPTY_JSON_STRING = "{}";

    // Logging
    private final Logger archiveIngestorServiceLogger;

    public ArchiveIngestorService(@Value("${archiveingestor.driver.socket}") String driverSocket) throws IOException {
        this.archiveIngestor = new ArchiveIngestor();
        this.driverSocket = driverSocket;
        this.archiveIngestorServiceLogger = LoggerFactory.getLogger(ArchiveIngestorService.class);
        this.archiveIngestorServiceLogger.info("Driver socket is set to {}.", this.driverSocket);
    }

    public ArchiveIngestorInfo getArchiveIngestorInfo() {
        JSONArray supportedOTWArchiveVersions = this.archiveIngestor.versionTable.getJSONArray(ArchiveIngestor.VERSION);
        String lastSupportedOTWArchiveVersion = supportedOTWArchiveVersions.toList().getLast().toString();
        this.archiveIngestorServiceLogger.info(
                "Sending version number {} and OTW Archive version {}",
                ArchiveIngestor.VERSION, lastSupportedOTWArchiveVersion
        );
        return new ArchiveIngestorInfo(ArchiveIngestor.VERSION, lastSupportedOTWArchiveVersion);
    }

    public ArchiveIngestorResponse parseChapter(ArchiveIngestorRequest request) {
        // Extract request items
        URL chapterURL = request.getPageLinkURL();
        StoryInfo storyInfo = request.getStoryInfo();
        this.archiveIngestorServiceLogger.info("Successfully obtained parseChapter request contents.");

        // Create a Selenium driver
        ChromeOptions options = new ChromeOptions();
        URL containerLocator;

        try {
            URI containerIdentifier = new URI(this.driverSocket);
            containerLocator = containerIdentifier.toURL();
        }
        catch (URISyntaxException | MalformedURLException e) {
            this.archiveIngestorServiceLogger.error(
                    "Failed to create URL with driver address {} for parseChapter.", this.driverSocket
            );
            return new ArchiveIngestorResponse(this.EMPTY_JSON_STRING, "Failed to create URL with driver address.");
        }

        RemoteWebDriver driver = new RemoteWebDriver(containerLocator, options);
        this.archiveIngestorServiceLogger.info("Successfully created driver for parseChapter.");

        // Navigate to the chapter URL
        driver.get(chapterURL.toString());

        // Create a chapter object
        Chapter newChapter;

        try {
            if (storyInfo == null) {
                newChapter = this.archiveIngestor.createChapter(driver);
            } else {
                newChapter = this.archiveIngestor.createChapter(driver, storyInfo);
            }
            this.archiveIngestorServiceLogger.info("Successfully parsed link and extracted chapter.");
        }
        catch (InterruptedException e) {
            this.archiveIngestorServiceLogger.error(
                    "Execution was unexpectedly interrupted during Thread.sleep() in parseChapter."
            );
            return new ArchiveIngestorResponse(
                    this.EMPTY_JSON_STRING, "Execution was unexpectedly interrupted during Thread.sleep()."
            );
        }

        // End the driver session
        driver.quit();
        this.archiveIngestorServiceLogger.info("Successfully quit driver for parseChapter.");

        // Return the chapter's contents
        JSONObject newChapterJSON = newChapter.getJSONRepWithParent();
        this.archiveIngestorServiceLogger.info("Successfully retrieved JSON representation of new chapter.");

        return new ArchiveIngestorResponse(newChapterJSON.toString(), "Chapter parsing was successful.");
    }

    public ArchiveIngestorResponse parseStory(ArchiveIngestorRequest request) {
        // Extract request items
        URL storyURL = request.getPageLinkURL();
        this.archiveIngestorServiceLogger.info("Successfully obtained parseStory request contents.");

        // Create a Selenium driver
        ChromeOptions options = new ChromeOptions();
        URL containerLocator;

        try {
            URI containerIdentifier = new URI(this.driverSocket);
            containerLocator = containerIdentifier.toURL();
        }
        catch (URISyntaxException | MalformedURLException e) {
            this.archiveIngestorServiceLogger.error(
                    "Failed to create URL with driver address {} for parseStory.", this.driverSocket
            );
            return new ArchiveIngestorResponse(this.EMPTY_JSON_STRING, "Failed to create URL with requested link.");
        }

        RemoteWebDriver driver = new RemoteWebDriver(containerLocator, options);
        this.archiveIngestorServiceLogger.info("Successfully created driver for parseStory.");

        // Navigate to the chapter URL
        driver.get(storyURL.toString());

        // Create a story object
        Story newStory;
        try {
            newStory = this.archiveIngestor.createStory(driver);
            this.archiveIngestorServiceLogger.info("Successfully parsed link and extracted story.");
        }
        catch (InterruptedException e) {
            this.archiveIngestorServiceLogger.error(
                    "Execution was unexpectedly interrupted during Thread.sleep() in parseStory."
            );
            return new ArchiveIngestorResponse(
                    this.EMPTY_JSON_STRING, "Execution was unexpectedly interrupted during Thread.sleep()."
            );
        }

        // End the driver session
        driver.quit();
        this.archiveIngestorServiceLogger.info("Successfully quit driver for parseStory.");

        // Return the chapter's contents
        JSONObject newStoryJSON = newStory.getJSONRep();
        this.archiveIngestorServiceLogger.info("Successfully retrieved JSON representation of new story.");

        return new ArchiveIngestorResponse(newStoryJSON.toString(), "Story parsing was successful.");
    }
}
