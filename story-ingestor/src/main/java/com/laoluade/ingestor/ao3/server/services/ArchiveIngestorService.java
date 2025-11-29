package com.laoluade.ingestor.ao3.server.services;

// Local classes
import com.laoluade.ingestor.ao3.core.ArchiveIngestor;
import com.laoluade.ingestor.ao3.core.Chapter;
import com.laoluade.ingestor.ao3.core.Story;
import com.laoluade.ingestor.ao3.core.StoryInfo;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorRequest;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorResponse;

// Third party classes
import org.json.JSONObject;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Java classes
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Service
public class ArchiveIngestorService {
    // Attributes
    private final ArchiveIngestor archiveIngestor;
    private final String driverSocket;

    // Constants
    private final String EMPTY_JSON_STRING = "{}";

    public ArchiveIngestorService(@Value("${archiveingestor.driver.socket}") String driverSocket) throws IOException {
        this.archiveIngestor = new ArchiveIngestor();
        this.driverSocket = driverSocket;
    }

    public ArchiveIngestorResponse parseChapter(ArchiveIngestorRequest request) {
        // Extract request items
        URL chapterURL = request.getPageLinkURL();
        StoryInfo storyInfo = request.getStoryInfo();

        // Create a Selenium driver
        ChromeOptions options = new ChromeOptions();
        URL containerLocator;

        try {
            URI containerIdentifier = new URI(this.driverSocket);
            containerLocator = containerIdentifier.toURL();
        }
        catch (URISyntaxException | MalformedURLException e) {
            return new ArchiveIngestorResponse(this.EMPTY_JSON_STRING, "Failed to create URL with requested link.");
        }

        RemoteWebDriver driver = new RemoteWebDriver(containerLocator, options);

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
        }
        catch (InterruptedException e) {
            return new ArchiveIngestorResponse(
                    this.EMPTY_JSON_STRING, "Execution was unexpectedly interrupted during Thread.sleep()."
            );
        }

        // End the driver session
        driver.quit();

        // Return the chapter's contents
        JSONObject newChapterJSON = newChapter.getJSONRepWithParent();
        return new ArchiveIngestorResponse(newChapterJSON.toString(), "Chapter parsing was successful.");
    }

    public ArchiveIngestorResponse parseStory(ArchiveIngestorRequest request) {
        // Extract request items
        URL storyURL = request.getPageLinkURL();

        // Create a Selenium driver
        ChromeOptions options = new ChromeOptions();
        URL containerLocator;

        try {
            URI containerIdentifier = new URI(this.driverSocket);
            containerLocator = containerIdentifier.toURL();
        }
        catch (URISyntaxException | MalformedURLException e) {
            return new ArchiveIngestorResponse(this.EMPTY_JSON_STRING, "Failed to create URL with requested link.");
        }

        RemoteWebDriver driver = new RemoteWebDriver(containerLocator, options);

        // Navigate to the chapter URL
        driver.get(storyURL.toString());

        // Create a story object
        Story newStory;
        try {
            newStory = this.archiveIngestor.createStory(driver);
        }
        catch (InterruptedException e) {
            return new ArchiveIngestorResponse(
                    this.EMPTY_JSON_STRING, "Execution was unexpectedly interrupted during Thread.sleep()."
            );
        }

        // End the driver session
        driver.quit();

        // Return the chapter's contents
        JSONObject newStoryJSON = newStory.getJSONRep();
        return new ArchiveIngestorResponse(newStoryJSON.toString(), "Story parsing was successful.");
    }
}
