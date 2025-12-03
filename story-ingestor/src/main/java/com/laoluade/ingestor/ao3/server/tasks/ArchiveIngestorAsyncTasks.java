package com.laoluade.ingestor.ao3.server.tasks;

// Core Package
import com.laoluade.ingestor.ao3.core.ArchiveIngestor;
import com.laoluade.ingestor.ao3.core.Chapter;
import com.laoluade.ingestor.ao3.core.Story;

// Error Package
import com.laoluade.ingestor.ao3.errors.ChapterContentNotFoundError;
import com.laoluade.ingestor.ao3.errors.IngestorCanceledError;
import com.laoluade.ingestor.ao3.errors.IngestorElementNotFoundError;

// Server Model Package
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorResponse;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorTaskFuture;

// 3rd-party Packages
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

// Java Packages
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

@Component
public class ArchiveIngestorAsyncTasks {
    public CompletableFuture<ArchiveIngestorTaskFuture> returnFailedFuture(String resultMessage, Logger logger,
                                                                           ArchiveIngestorResponse curResponse) {
        // Log error
        logger.error(resultMessage);

        // Set the response instance with the chapter's contents
        curResponse.setResultMessage(resultMessage);
        curResponse.getSessionInfo().setIsFinished(true);
        curResponse.getSessionInfo().setIsCanceled(true);
        curResponse.getSessionInfo().refreshCreationTimestamp();

        // Return failed future
        return CompletableFuture.completedFuture(new ArchiveIngestorTaskFuture(resultMessage, false));
    }

    public CompletableFuture<ArchiveIngestorTaskFuture> returnCompletedFuture(String newChapterJSONString,
                                                                              String resultMessage, Logger logger,
                                                                              ArchiveIngestorResponse curResponse) {
        // Log info
        logger.info(resultMessage);

        // Set the response instance with the chapter's contents
        curResponse.setResponseJSONString(newChapterJSONString);
        curResponse.setResultMessage(resultMessage);
        curResponse.getSessionInfo().setIsFinished(true);
        curResponse.getSessionInfo().refreshCreationTimestamp();

        // Return the completed future
        return CompletableFuture.completedFuture(new ArchiveIngestorTaskFuture(resultMessage, true));
    }

    @Async("archiveIngestorAsyncExecutor")
    public CompletableFuture<ArchiveIngestorTaskFuture> parseChapter(String driverSocket, Logger logger, URL chapterURL,
                                                                     ArchiveIngestorResponse curResponse) {
        // Create a Selenium driver
        ChromeOptions options = new ChromeOptions();
        URL containerLocator;

        try {
            URI containerIdentifier = new URI(driverSocket);
            containerLocator = containerIdentifier.toURL();
        }
        catch (URISyntaxException | MalformedURLException e) {
            String resultMessage = "Failed to create URL with driver address " + driverSocket + " for chapter parsing.";
            return returnFailedFuture(resultMessage, logger, curResponse);
        }

        RemoteWebDriver driver = new RemoteWebDriver(containerLocator, options);
        logger.info("Successfully created driver for chapter parsing.");

        // Navigate to the chapter URL
        driver.get(chapterURL.toString());

        // Create a chapter object
        Chapter newChapter;

        try {
            ArchiveIngestor archiveIngestor = new ArchiveIngestor(curResponse.getSessionInfo());
            newChapter = archiveIngestor.createChapter(driver);
            logger.info("Successfully parsed link and extracted chapter.");
        }
        catch (IOException e) {
            String resultMessage = "The archive ingestor could not read in json settings.";
            return returnFailedFuture(resultMessage, logger, curResponse);
        }
        catch (InterruptedException e) {
            String resultMessage = "The execution was unexpectedly interrupted during Thread.sleep() during chapter parsing.";
            return returnFailedFuture(resultMessage, logger, curResponse);
        }
        catch (ChapterContentNotFoundError e) {
            String resultMessage = "The chapter's paragraphs could not be found.";
            return returnFailedFuture(resultMessage, logger, curResponse);
        }
        catch (IngestorElementNotFoundError e) {
            String resultMessage = "The archive ingestor could not find a required element during parsing.";
            return returnFailedFuture(resultMessage, logger, curResponse);
        }
        catch (IngestorCanceledError e) {
            String resultMessage = "The archive ingestor's task was canceled from parent service.";
            return returnFailedFuture(resultMessage, logger, curResponse);
        }

        // End the driver session
        driver.quit();
        logger.info("Successfully quit driver for chapter parsing.");

        // Set the response instance with the chapter's contents and return everything
        String newChapterJSONString = newChapter.getJSONRepWithParent().toString();
        String resultMessage = "Successfully retrieved JSON representation of new chapter.";
        return returnCompletedFuture(newChapterJSONString, resultMessage, logger, curResponse);
    }

    @Async("archiveIngestorAsyncExecutor")
    public CompletableFuture<ArchiveIngestorTaskFuture> parseStory(String driverSocket, Logger logger, URL storyURL,
                                                                     ArchiveIngestorResponse curResponse) {
        // Create a Selenium driver
        ChromeOptions options = new ChromeOptions();
        URL containerLocator;

        try {
            URI containerIdentifier = new URI(driverSocket);
            containerLocator = containerIdentifier.toURL();
        }
        catch (URISyntaxException | MalformedURLException e) {
            String resultMessage = "Failed to create URL with driver address " + driverSocket + " for story parsing.";
            return returnFailedFuture(resultMessage, logger, curResponse);
        }

        RemoteWebDriver driver = new RemoteWebDriver(containerLocator, options);
        logger.info("Successfully created driver for story parsing.");

        // Navigate to the story URL
        driver.get(storyURL.toString());

        // Create a story object
        Story newStory;

        try {
            ArchiveIngestor archiveIngestor = new ArchiveIngestor(curResponse.getSessionInfo());
            newStory = archiveIngestor.createStory(driver);
            logger.info("Successfully parsed link and extracted story.");
        }
        catch (InterruptedException e) {
            String resultMessage = "Execution was unexpectedly interrupted during Thread.sleep() during story parsing.";
            return returnFailedFuture(resultMessage, logger, curResponse);
        }
        catch (IOException e) {
            String resultMessage = "Archive ingestor could not read in json settings.";
            return returnFailedFuture(resultMessage, logger, curResponse);
        }
        catch (ChapterContentNotFoundError e) {
            String resultMessage = "One of the chapter's paragraphs could not be found.";
            return returnFailedFuture(resultMessage, logger, curResponse);
        }
        catch (IngestorElementNotFoundError e) {
            String resultMessage = "The archive ingestor could not find a required element during parsing.";
            return returnFailedFuture(resultMessage, logger, curResponse);
        }
        catch (IngestorCanceledError e) {
            String resultMessage = "The archive ingestor's task was canceled from parent service.";
            return returnFailedFuture(resultMessage, logger, curResponse);
        }

        // End the driver session
        driver.quit();
        logger.info("Successfully quit driver for story parsing.");

        // Set the response instance with the story's contents and return everything
        String newStoryJSONString = newStory.getJSONRep().toString();
        String resultMessage = "Successfully retrieved JSON representation of new story.";
        return returnCompletedFuture(newStoryJSONString, resultMessage, logger, curResponse);
    }
}
