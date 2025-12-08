package com.laoluade.ingestor.ao3.services;

// Core Classes
import com.laoluade.ingestor.ao3.core.ArchiveIngestor;
import com.laoluade.ingestor.ao3.core.Chapter;
import com.laoluade.ingestor.ao3.core.Story;

// Error Classes
import com.laoluade.ingestor.ao3.errors.ChapterContentNotFoundError;
import com.laoluade.ingestor.ao3.errors.IngestorCanceledError;
import com.laoluade.ingestor.ao3.errors.IngestorElementNotFoundError;

// Server Model Classes
import com.laoluade.ingestor.ao3.models.ArchiveIngestorResponse;
import com.laoluade.ingestor.ao3.repositories.ArchiveIngestorSessionEntity;
import com.laoluade.ingestor.ao3.models.ArchiveIngestorSessionInfo;
import com.laoluade.ingestor.ao3.models.ArchiveIngestorTaskFuture;

// 3rd-party Classes
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

// Java Classes
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

@Component
public class ArchiveIngestorAsyncTaskManager {
    @Autowired
    private final ArchiveIngestor archiveIngestor;

    @Autowired
    private final ArchiveIngestorLogManager logManager;

    @Autowired
    private final ArchiveIngestorMessageManager messageManager;

    @Autowired
    private final ArchiveIngestorSessionManager sessionManager;

    public ArchiveIngestorAsyncTaskManager(ArchiveIngestor archiveIngestor,
                                           ArchiveIngestorLogManager logManager,
                                           ArchiveIngestorMessageManager messageManager,
                                           ArchiveIngestorSessionManager sessionManager) {
        this.archiveIngestor = archiveIngestor;
        this.logManager = logManager;
        this.messageManager = messageManager;
        this.sessionManager = sessionManager;
    }

    public CompletableFuture<ArchiveIngestorTaskFuture> returnFailedFuture(String resultMessage, String sessionID) {
        // Log error
        this.logManager.createErrorLog(resultMessage);

        // Create session objects
        ArchiveIngestorSessionEntity curSession = this.sessionManager.getSession(sessionID);
        ArchiveIngestorResponse curResponse = curSession.getSessionResponse();
        ArchiveIngestorSessionInfo curSessionInfo = curResponse.getSessionInfo();

        // Only set the finished flag if it's not a user-initiated canceling
        if (!resultMessage.equals(this.messageManager.getLoggingErrorParseFailedCanceled())) {
            curSessionInfo.setIsFinished(true);
        }

        // Set the rest of the response instance with the chapter's contents
        curSessionInfo.setIsCanceled(true);
        curSessionInfo.refreshCreationTimestamp();

        curResponse.setSessionInfo(curSessionInfo);
        curResponse.setResultMessage(resultMessage);
        this.sessionManager.updateSession(sessionID, curResponse);

        // Return failed future
        return CompletableFuture.completedFuture(new ArchiveIngestorTaskFuture(resultMessage, false));
    }

    public CompletableFuture<ArchiveIngestorTaskFuture> returnCompletedFuture(String newChapterJSONString,
                                                                              String resultMessage, String sessionID) {
        // Log info
        this.logManager.createInfoLog(resultMessage);

        // Create session objects
        ArchiveIngestorSessionEntity curSession = this.sessionManager.getSession(sessionID);
        ArchiveIngestorResponse curResponse = curSession.getSessionResponse();
        ArchiveIngestorSessionInfo curSessionInfo = curResponse.getSessionInfo();

        // Set the response instance with the chapter's contents
        curSessionInfo.setIsFinished(true);
        curSessionInfo.refreshCreationTimestamp();

        curResponse.setResponseJSONString(newChapterJSONString);
        curResponse.setSessionInfo(curSessionInfo);
        curResponse.setResultMessage(resultMessage);
        this.sessionManager.updateSession(sessionID, curResponse);

        // Return the completed future
        return CompletableFuture.completedFuture(new ArchiveIngestorTaskFuture(resultMessage, true));
    }

    @Async("archiveIngestorAsyncExecutor")
    public CompletableFuture<ArchiveIngestorTaskFuture> parseChapter(String driverSocket, String chapterLink, 
                                                                     String sessionID) {
        // Create a Selenium driver
        ChromeOptions options = new ChromeOptions();
        URL containerLocator;

        try {
            URI containerIdentifier = new URI(driverSocket);
            containerLocator = containerIdentifier.toURL();
        }
        catch (URISyntaxException | MalformedURLException e) {
            return returnFailedFuture(
                    this.messageManager.createChapterURLExceptionMessage(driverSocket), sessionID
            );
        }

        RemoteWebDriver driver = new RemoteWebDriver(containerLocator, options);
        this.logManager.createInfoLog(this.messageManager.getLoggingInfoChapterCreatedDriver());

        // Navigate to the chapter URL
        driver.get(chapterLink);

        // Create a chapter object
        Chapter newChapter;

        try {
            newChapter = this.archiveIngestor.createChapter(driver, sessionID);
            this.logManager.createInfoLog(this.messageManager.getLoggingInfoChapterParseSucceeded());
        }
        catch (InterruptedException e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorChapterFailedInterrupt(), sessionID);
        }
        catch (ChapterContentNotFoundError e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorChapterFailedContent(), sessionID);
        }
        catch (IngestorElementNotFoundError e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorParseFailedElement(), sessionID);
        }
        catch (IngestorCanceledError e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorParseFailedCanceled(), sessionID);
        }

        // End the driver session
        driver.quit();
        this.logManager.createInfoLog(this.messageManager.getLoggingInfoChapterQuitDriver());

        // Set the response instance with the chapter's contents and return everything
        String newChapterJSONString = newChapter.getJSONRepWithParent().toString();
        return returnCompletedFuture(
                newChapterJSONString, this.messageManager.getLoggingInfoChapterRetrievedJSON(), sessionID
        );
    }

    @Async("archiveIngestorAsyncExecutor")
    public CompletableFuture<ArchiveIngestorTaskFuture> parseStory(String driverSocket, String storyLink,
                                                                   String sessionID) {
        // Create a Selenium driver
        ChromeOptions options = new ChromeOptions();
        URL containerLocator;

        try {
            URI containerIdentifier = new URI(driverSocket);
            containerLocator = containerIdentifier.toURL();
        }
        catch (URISyntaxException | MalformedURLException e) {
            return returnFailedFuture(
                    this.messageManager.createStoryURLExceptionMessage(driverSocket), sessionID
            );
        }

        RemoteWebDriver driver = new RemoteWebDriver(containerLocator, options);
        this.logManager.createInfoLog(this.messageManager.getLoggingInfoStoryCreatedDriver());

        // Navigate to the story URL
        driver.get(storyLink);

        // Create a story object
        Story newStory;

        try {
            newStory = this.archiveIngestor.createStory(driver, sessionID);
            this.logManager.createInfoLog(this.messageManager.getLoggingInfoStoryParseSucceeded());
        }
        catch (InterruptedException e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorStoryFailedInterrupt(), sessionID);
        }
        catch (ChapterContentNotFoundError e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorStoryFailedContent(), sessionID);
        }
        catch (IngestorElementNotFoundError e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorParseFailedElement(), sessionID);
        }
        catch (IngestorCanceledError e) {
            return returnFailedFuture(this.messageManager.getLoggingErrorParseFailedCanceled(), sessionID);
        }

        // End the driver session
        driver.quit();
        this.logManager.createInfoLog(this.messageManager.getLoggingInfoStoryQuitDriver());

        // Set the response instance with the story's contents and return everything
        String newStoryJSONString = newStory.getJSONRep().toString();
        return returnCompletedFuture(
                newStoryJSONString, this.messageManager.getLoggingInfoStoryRetrievedJSON(), sessionID
        );
    }
}
