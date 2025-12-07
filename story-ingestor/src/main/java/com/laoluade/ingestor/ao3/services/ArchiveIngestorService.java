package com.laoluade.ingestor.ao3.services;

// Local Classes
import com.laoluade.ingestor.ao3.core.ArchiveIngestor;
import com.laoluade.ingestor.ao3.models.*;

// Third-party Classes
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Java Classes
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;

@Service
public class ArchiveIngestorService {
    // Manager components
    @Autowired
    private final ArchiveIngestor archiveIngestor;
    
    @Autowired
    private final ArchiveIngestorAsyncTaskManager asyncTaskManager;
    
    @Autowired
    private final ArchiveIngestorLogManager logManager;

    @Autowired
    private final ArchiveIngestorMessageManager messageManager;

    @Autowired
    private final ArchiveIngestorSessionManager sessionManager;

    // Attributes and constants
    private final String driverSocket;

    public ArchiveIngestorService(@Value("${archiveIngestor.driver.socket}") String driverSocket,
                                  ArchiveIngestor archiveIngestor,
                                  ArchiveIngestorAsyncTaskManager asyncTaskManager,
                                  ArchiveIngestorLogManager logManager,
                                  ArchiveIngestorMessageManager messageManager,
                                  ArchiveIngestorSessionManager sessionManager) throws InterruptedException {
        // Initialize attributes
        this.driverSocket = driverSocket;

        // Initialize components
        this.archiveIngestor = archiveIngestor;
        this.asyncTaskManager = asyncTaskManager;
        this.logManager = logManager;
        this.messageManager = messageManager;
        this.sessionManager = sessionManager;
        this.sessionManager.sessionValidityMonitor();
    }

    public ArchiveIngestorTestAPIInfo getArchiveIngestorTestAPI() {
        this.logManager.createInfoLog(this.messageManager.getLoggingInfoTestAPISend());
        return new ArchiveIngestorTestAPIInfo(this.messageManager.getTestAPIInfo());
    }

    // TODO: Add service versioning information
    public ArchiveIngestorInfo getArchiveIngestorInfo() {
        try {
            this.logManager.createInfoLog(this.messageManager.createInfoSendingMessage(
                    this.archiveIngestor.getArchiveIngestorVersion(), this.archiveIngestor.getOTWArchiveVersion()
            ));
            return new ArchiveIngestorInfo(
                    this.archiveIngestor.getArchiveIngestorVersion(), this.archiveIngestor.getOTWArchiveVersion()
            );
        } catch (Exception e) {
            return new ArchiveIngestorInfo(
                    this.messageManager.getInfoGenericFailValue(), this.messageManager.getInfoGenericFailValue()
            );
        }
    }

    public ArchiveIngestorResponse startParseChapter(ArchiveIngestorRequest request) {
        // Extract request items
        String chapterLink = request.getPageLink();
        String sessionNickname = request.getSessionNickname();
        this.logManager.createInfoLog(this.messageManager.getLoggingInfoChapterObtainRequest());

        // Check if chapter link passed inspection
        if (chapterLink.isEmpty()) {
            return new ArchiveIngestorResponse(
                    this.messageManager.getEmptyValue(), this.messageManager.getResponseBadURLFormat()
            );
        }

        // Create a session ID
        String timestamp = ZonedDateTime.now(ZoneId.of(this.messageManager.getUTCValue())).toString();
        String hashString = chapterLink + timestamp;
        String newSessionID = Hashing.sha256().hashString(hashString, StandardCharsets.UTF_8).toString();

        // Re-set the nickname if needed
        if (sessionNickname.isEmpty()) {
            sessionNickname = newSessionID;
        }

        // Create response object and start filling it in
        ArchiveIngestorSessionInfo newSessionInfo = new ArchiveIngestorSessionInfo(newSessionID, sessionNickname);
        ArchiveIngestorResponse newResponse = new ArchiveIngestorResponse(
                this.messageManager.getEmptyValue(), this.messageManager.getResponseNewChapterSession(), newSessionInfo
        );

        // Start the chapter parsing process
        CompletableFuture<ArchiveIngestorTaskFuture> newFuture = this.asyncTaskManager.parseChapter(
                this.driverSocket, chapterLink, newSessionID
        );

        // Add session to session manager
        this.sessionManager.addSession(newSessionID, new ArchiveIngestorSession(newResponse, newFuture));

        // Return the response
        return newResponse;
    }

    public ArchiveIngestorResponse startParseStory(ArchiveIngestorRequest request) {
        // Extract request items
        String storyLink = request.getPageLink();
        String sessionNickname = request.getSessionNickname();
        this.logManager.createInfoLog(this.messageManager.getLoggingInfoStoryObtainRequest());

        // Check if story link passed inspection
        if (storyLink.isEmpty()) {
            return new ArchiveIngestorResponse(
                    this.messageManager.getEmptyValue(), this.messageManager.getResponseBadURLFormat()
            );
        }

        // Create a session ID
        String timestamp = ZonedDateTime.now(ZoneId.of(this.messageManager.getUTCValue())).toString();
        String hashString = storyLink + timestamp;
        String newSessionID = Hashing.sha256().hashString(hashString, StandardCharsets.UTF_8).toString();

        // Re-set the nickname if needed
        if (sessionNickname.isEmpty()) {
            sessionNickname = newSessionID;
        }

        // Create response object and start filling it in
        ArchiveIngestorSessionInfo newSessionInfo = new ArchiveIngestorSessionInfo(newSessionID, sessionNickname);
        ArchiveIngestorResponse newResponse = new ArchiveIngestorResponse(
                this.messageManager.getEmptyValue(), this.messageManager.getResponseNewStorySession(), newSessionInfo
        );

        // Start the story parsing process
        CompletableFuture<ArchiveIngestorTaskFuture> newFuture = this.asyncTaskManager.parseStory(
                this.driverSocket, storyLink, newSessionID
        );

        // Add session to session manager
        this.sessionManager.addSession(newSessionID, new ArchiveIngestorSession(newResponse, newFuture));

        // Return the response
        return newResponse;
    }

    public ArchiveIngestorResponse getSessionInformation(String sessionID) {
        // Get the session information or send a failed message
        ArchiveIngestorSession session = this.sessionManager.getSession(sessionID);
        if (session != null) {
            return session.getSessionResponse();
        }
        else {
            return new ArchiveIngestorResponse(
                    this.messageManager.getEmptyValue(), this.messageManager.getResponseGetSessionFailed()
            );
        }
    }

    public ArchiveIngestorResponse cancelSession(String sessionID) {
        // Cancel the session's async task
        boolean success = this.sessionManager.cancelSession(sessionID);
        if (success) {
            return new ArchiveIngestorResponse(
                    this.messageManager.getEmptyValue(), this.messageManager.getResponseCancelSucceeded()
            );
        }
        else {
            return new ArchiveIngestorResponse(
                    this.messageManager.getEmptyValue(), this.messageManager.getResponseCancelFailed()
            );
        }
    }
}
