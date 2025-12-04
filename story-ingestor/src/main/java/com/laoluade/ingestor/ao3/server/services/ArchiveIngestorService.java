package com.laoluade.ingestor.ao3.server.services;

// Local classes
import com.laoluade.ingestor.ao3.core.ArchiveIngestor;
import com.laoluade.ingestor.ao3.server.ArchiveIngestorMessageManager;
import com.laoluade.ingestor.ao3.server.models.*;
import com.laoluade.ingestor.ao3.server.tasks.ArchiveIngestorAsyncTaskManager;

// Third party classes
import com.google.common.hash.Hashing;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Java classes
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Service
public class ArchiveIngestorService {
    // Manager components
    @Autowired
    private final ArchiveIngestorAsyncTaskManager asyncTaskManager;

    @Autowired
    private final ArchiveIngestorMessageManager messageManager;

    // Attributes
    private final String driverSocket;
    private final Integer sessionPersistSecs;
    private final HashMap<String, ArchiveIngestorSession> sessionManager;

    // Constants
    private final String UTC = "UTC";
    private final String EMPTY_STRING = "";

    // Logging
    private final Logger archiveIngestorServiceLogger;

    public ArchiveIngestorService(@Value("${archiveIngestor.driver.socket}") String driverSocket,
                                  @Value("${archiveIngestor.session.persistSecs:120}") Integer sessionPersistSecs,
                                  ArchiveIngestorAsyncTaskManager asyncTaskManager,
                                  ArchiveIngestorMessageManager messageManager) {
        // Initialize attributes
        this.driverSocket = driverSocket;
        this.sessionPersistSecs = sessionPersistSecs;
        this.sessionManager = new HashMap<String, ArchiveIngestorSession>();

        // Initialize component manager
        this.asyncTaskManager = asyncTaskManager;
        this.messageManager = messageManager;

        // Start logger
        this.archiveIngestorServiceLogger = LoggerFactory.getLogger(ArchiveIngestorService.class);
        this.archiveIngestorServiceLogger.info(this.messageManager.createDriverSocketMessage(this.driverSocket));
        this.archiveIngestorServiceLogger.info(this.messageManager.createSessionPersistSecsMessage(this.sessionPersistSecs));
    }

    public ArchiveIngestorTestAPIInfo getArchiveIngestorTestAPI() {
        this.archiveIngestorServiceLogger.info(this.messageManager.getLoggingInfoTestAPISend());
        return new ArchiveIngestorTestAPIInfo(this.messageManager.getTestAPIInfo());
    }

    public ArchiveIngestorInfo getArchiveIngestorInfo() {
        try {
            JSONArray supportedOTWArchiveVersions = new ArchiveIngestor().versionTable.getJSONArray(ArchiveIngestor.VERSION);
            String lastSupportedOTWArchiveVersion = supportedOTWArchiveVersions.toList().getLast().toString();
            this.archiveIngestorServiceLogger.info(
                    this.messageManager.createInfoSendingMessage(ArchiveIngestor.VERSION, lastSupportedOTWArchiveVersion)
            );

            return new ArchiveIngestorInfo(ArchiveIngestor.VERSION, lastSupportedOTWArchiveVersion);
        }
        catch (IOException e) {
            return new ArchiveIngestorInfo(
                    this.messageManager.getInfoIOFailValue(), this.messageManager.getInfoIOFailValue()
            );
        }
        catch (Exception e) {
            return new ArchiveIngestorInfo(
                    this.messageManager.getInfoGenericFailValue(), this.messageManager.getInfoGenericFailValue()
            );
        }
    }

    public ArchiveIngestorResponse startParseChapter(ArchiveIngestorRequest request) {
        // Extract request items
        URL chapterURL = request.getPageLinkURL();
        String sessionNickname = request.getSessionNickname();
        this.archiveIngestorServiceLogger.info(this.messageManager.getLoggingInfoChapterObtainRequest());

        // Create a session ID
        String timestamp = ZonedDateTime.now(ZoneId.of(this.UTC)).toString();
        String hashString = chapterURL.toString() + timestamp;
        String newSessionID = Hashing.sha256().hashString(hashString, StandardCharsets.UTF_8).toString();

        // Create response object and start filling it in
        ArchiveIngestorSessionInfo newSessionInfo = new ArchiveIngestorSessionInfo(newSessionID, sessionNickname);
        ArchiveIngestorResponse newResponse = new ArchiveIngestorResponse(
                this.EMPTY_STRING, this.messageManager.getResponseNewChapterSession(), newSessionInfo
        );

        // Start the chapter parsing process
        CompletableFuture<ArchiveIngestorTaskFuture> newFuture = this.asyncTaskManager.parseChapter(
                this.driverSocket, this.archiveIngestorServiceLogger, chapterURL, newResponse
        );

        // Add session to session manager
        this.sessionManager.put(newSessionID, new ArchiveIngestorSession(newResponse, newFuture));

        // Return the response
        return newResponse;
    }

    public ArchiveIngestorResponse startParseStory(ArchiveIngestorRequest request) {
        // Extract request items
        URL storyURL = request.getPageLinkURL();
        String sessionNickname = request.getSessionNickname();
        this.archiveIngestorServiceLogger.info(this.messageManager.getLoggingInfoStoryObtainRequest());

        // Create a session ID
        String timestamp = ZonedDateTime.now(ZoneId.of(this.UTC)).toString();
        String hashString = storyURL.toString() + timestamp;
        String newSessionID = Hashing.sha256().hashString(hashString, StandardCharsets.UTF_8).toString();

        // Create response object and start filling it in
        ArchiveIngestorSessionInfo newSessionInfo = new ArchiveIngestorSessionInfo(newSessionID, sessionNickname);
        ArchiveIngestorResponse newResponse = new ArchiveIngestorResponse(
                this.EMPTY_STRING, this.messageManager.getResponseNewStorySession(), newSessionInfo
        );

        // Start the story parsing process
        CompletableFuture<ArchiveIngestorTaskFuture> newFuture = this.asyncTaskManager.parseStory(
                this.driverSocket, this.archiveIngestorServiceLogger, storyURL, newResponse
        );

        // Add session to session manager
        this.sessionManager.put(newSessionID, new ArchiveIngestorSession(newResponse, newFuture));

        // Return the response
        return newResponse;
    }

    public void purgeSessionManager() {
        ArrayList<String> sessionsToDelete = new ArrayList<>();
        ZonedDateTime currentTimestamp = ZonedDateTime.now(ZoneId.of(this.UTC));

        // Get session IDs to delete
        for (HashMap.Entry<String, ArchiveIngestorSession> sessionEntry : this.sessionManager.entrySet()) {
            ArchiveIngestorSessionInfo sessionInfo = sessionEntry.getValue().getSessionResponse().getSessionInfo();
            ZonedDateTime lastSessionTimestamp = ZonedDateTime.parse(sessionInfo.getCreationTimestamp());
            ZonedDateTime lastValidSessionTime = lastSessionTimestamp.plusSeconds(this.sessionPersistSecs);

            if (currentTimestamp.isAfter(lastValidSessionTime)) {
                sessionsToDelete.add(sessionEntry.getKey());
            }
        }

        // Delete sessions by their session ID
        for (String sessionID : sessionsToDelete) {
            this.sessionManager.remove(sessionID);
        }
    }

    public ArchiveIngestorResponse getSessionInformation(String sessionID) {
        // Purge the session manager
        purgeSessionManager();

        // Get the session information or send a failed message
        ArchiveIngestorSession session = this.sessionManager.get(sessionID);
        if (session != null) {
            return session.getSessionResponse();
        }
        else {
            return new ArchiveIngestorResponse(this.EMPTY_STRING, this.messageManager.getResponseGetSessionFailed());
        }
    }

    public ArchiveIngestorResponse cancelSession(String sessionID) {
        // Purge the session manager
        purgeSessionManager();

        // Cancel the session's async task
        ArchiveIngestorSession session = this.sessionManager.get(sessionID);
        if (session != null) {
            session.getSessionFuture().cancel(true);
            return new ArchiveIngestorResponse(this.EMPTY_STRING, this.messageManager.getResponseCancelSucceeded());
        }
        else {
            return new ArchiveIngestorResponse(this.EMPTY_STRING, this.messageManager.getResponseCancelFailed());
        }
    }
}
