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
import com.laoluade.ingestor.ao3.server.ArchiveIngestorMessageManager;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorResponse;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorTaskFuture;

// 3rd-party Packages
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ArchiveIngestorAsyncTaskManager {
    @Autowired
    private final ArchiveIngestorMessageManager messageManager;

    public ArchiveIngestorAsyncTaskManager(ArchiveIngestorMessageManager messageManager) {
        this.messageManager = messageManager;
    }

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
            return returnFailedFuture(
                    this.messageManager.createChapterURLExceptionMessage(driverSocket), logger, curResponse
            );
        }

        RemoteWebDriver driver = new RemoteWebDriver(containerLocator, options);
        logger.info(this.messageManager.getLoggingInfoChapterCreatedDriver());

        // Navigate to the chapter URL
        driver.get(chapterURL.toString());

        // Create a chapter object
        Chapter newChapter;

        try {
            ArchiveIngestor archiveIngestor = new ArchiveIngestor(curResponse.getSessionInfo());
            newChapter = archiveIngestor.createChapter(driver);
            logger.info(this.messageManager.getLoggingInfoChapterParseSucceeded());
        }
        catch (IOException e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorParseFailedIO(), logger, curResponse);
        }
        catch (InterruptedException e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorChapterFailedInterrupt(), logger, curResponse);
        }
        catch (ChapterContentNotFoundError e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorChapterFailedContent(), logger, curResponse);
        }
        catch (IngestorElementNotFoundError e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorParseFailedElement(), logger, curResponse);
        }
        catch (IngestorCanceledError e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorParseFailedCanceled(), logger, curResponse);
        }

        // End the driver session
        driver.quit();
        logger.info(this.messageManager.getLoggingInfoChapterQuitDriver());

        // Set the response instance with the chapter's contents and return everything
        String newChapterJSONString = newChapter.getJSONRepWithParent().toString();
        return returnCompletedFuture(
                newChapterJSONString, this.messageManager.getLoggingInfoChapterRetrievedJSON(), logger, curResponse
        );
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
            return returnFailedFuture(
                    this.messageManager.createStoryURLExceptionMessage(driverSocket), logger, curResponse
            );
        }

        RemoteWebDriver driver = new RemoteWebDriver(containerLocator, options);
        logger.info(this.messageManager.getLoggingInfoStoryCreatedDriver());

        // Navigate to the story URL
        driver.get(storyURL.toString());

        // Create a story object
        Story newStory;

        try {
            ArchiveIngestor archiveIngestor = new ArchiveIngestor(curResponse.getSessionInfo());
            newStory = archiveIngestor.createStory(driver);
            logger.info(this.messageManager.getLoggingInfoStoryParseSucceeded());
        }
        catch (IOException e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorParseFailedIO(), logger, curResponse);
        }
        catch (InterruptedException e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorStoryFailedInterrupt(), logger, curResponse);
        }
        catch (ChapterContentNotFoundError e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorStoryFailedContent(), logger, curResponse);
        }
        catch (IngestorElementNotFoundError e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorParseFailedElement(), logger, curResponse);
        }
        catch (IngestorCanceledError e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorParseFailedCanceled(), logger, curResponse);
        }

        // End the driver session
        driver.quit();
        logger.info(this.messageManager.getLoggingInfoStoryQuitDriver());

        // Set the response instance with the story's contents and return everything
        String newStoryJSONString = newStory.getJSONRep().toString();
        return returnCompletedFuture(
                newStoryJSONString, this.messageManager.getLoggingInfoStoryRetrievedJSON(), logger, curResponse
        );
    }
}
