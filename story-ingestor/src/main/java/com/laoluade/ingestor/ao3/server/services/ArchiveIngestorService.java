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
import org.springframework.web.bind.annotation.RequestBody;

// Java classes
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Service
public class ArchiveIngestorService {
    private final ArchiveIngestor archiveIngestor;
    private final String driverSocket;

    public ArchiveIngestorService(@Value("${archiveingestor.driver.socket}") String driverSocket) throws IOException {
        this.archiveIngestor = new ArchiveIngestor();
        this.driverSocket = driverSocket;
    }

    public ArchiveIngestorResponse parseChapter(@RequestBody ArchiveIngestorRequest request) throws InterruptedException,
            URISyntaxException, MalformedURLException {
        // Extract request items
        URL chapterURL = request.getPageLinkURL();
        StoryInfo storyInfo = request.getStoryInfo();

        // Create a Selenium driver
        ChromeOptions options = new ChromeOptions();
        URI containerIdentifier = new URI(this.driverSocket);
        URL containerLocator = containerIdentifier.toURL();
        RemoteWebDriver driver = new RemoteWebDriver(containerLocator, options);

        // Navigate to the chapter URL
        driver.get(chapterURL.toString());

        // Create a chapter object
        Chapter newChapter;
        if (storyInfo == null) {
            newChapter = this.archiveIngestor.createChapter(driver);
        }
        else {
            newChapter = this.archiveIngestor.createChapter(driver, storyInfo);
        }

        // End the driver session
        driver.quit();

        // Return the chapter's contents
        JSONObject newChapterJSON = newChapter.getJSONRepWithParent();
        return new ArchiveIngestorResponse(newChapterJSON.toString());
    }

    public ArchiveIngestorResponse parseStory(@RequestBody ArchiveIngestorRequest request) throws InterruptedException,
            URISyntaxException, MalformedURLException {
        // Extract request items
        URL storyURL = request.getPageLinkURL();

        // Create a Selenium driver
        ChromeOptions options = new ChromeOptions();
        URI containerIdentifier = new URI(this.driverSocket);
        URL containerLocator = containerIdentifier.toURL();
        RemoteWebDriver driver = new RemoteWebDriver(containerLocator, options);

        // Navigate to the chapter URL
        driver.get(storyURL.toString());

        // Create a story object
        Story newStory = this.archiveIngestor.createStory(driver);

        // End the driver session
        driver.quit();

        // Return the chapter's contents
        JSONObject newStoryJSON = newStory.getJSONRep();
        return new ArchiveIngestorResponse(newStoryJSON.toString());
    }
}
