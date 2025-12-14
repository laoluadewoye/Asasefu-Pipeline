package com.laoluade.ingestor.ao3.services;

// Local Classes
import com.laoluade.ingestor.ao3.core.ArchiveIngestor;
import com.laoluade.ingestor.ao3.models.*;

// Third-party Classes
import com.google.common.hash.Hashing;
import com.laoluade.ingestor.ao3.repositories.ArchiveParse;
import com.laoluade.ingestor.ao3.repositories.ArchiveParseType;
import com.laoluade.ingestor.ao3.repositories.ArchiveSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Java Classes
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Service
public class ArchiveService {
    // Service components
    @Autowired
    private final ArchiveIngestor archiveIngestor;
    
    @Autowired
    private final ArchiveLogService logService;

    @Autowired
    private final ArchiveMessageService messageService;

    @Autowired
    private final ArchiveSessionService sessionService;

    public ArchiveService(ArchiveIngestor archiveIngestor, ArchiveLogService logService,
                          ArchiveMessageService messageService, ArchiveSessionService sessionService)
            throws InterruptedException {
        // Initialize components
        this.archiveIngestor = archiveIngestor;
        this.logService = logService;
        this.messageService = messageService;
        this.sessionService = sessionService;

        // Start task monitoring
        this.sessionService.sessionTaskMonitor();
    }

    public ArchiveServerTestData getArchiveIngestorTestData() {
        this.logService.createInfoLog(this.messageService.getLoggingInfoTestAPISend());
        return new ArchiveServerTestData(this.messageService.getTestDataValue());
    }

    public ArchiveServerSpecData getArchiveServerSpecData() {
        try {
            this.logService.createInfoLog(this.messageService.createSpecDataMessage(
                    this.archiveIngestor.getArchiveIngestorVersion(), this.archiveIngestor.getLatestOTWArchiveVersion()
            ));
            return new ArchiveServerSpecData(
                    this.archiveIngestor.getArchiveIngestorVersion(), this.archiveIngestor.getLatestOTWArchiveVersion()
            );
        } catch (Exception e) {
            return new ArchiveServerSpecData(
                    this.messageService.getInfoGenericFailValue(), this.messageService.getInfoGenericFailValue()
            );
        }
    }

    public ArchiveServerResponseData startParseChapter(ArchiveServerRequestData request) {
        // Extract request items
        String chapterLink = request.getPageLink();
        String sessionNickname = request.getSessionNickname();
        boolean nicknameSent = request.isNicknameSent();
        this.logService.createInfoLog(this.messageService.getLoggingInfoChapterObtainRequest());

        // Check if request passed inspection
        if (chapterLink.isEmpty()) {
            return new ArchiveServerResponseData(this.messageService.getResponseBadURLFormat());
        }
        if (nicknameSent && sessionNickname.isEmpty()) {
            return new ArchiveServerResponseData(this.messageService.getResponseBadNicknameFormat());
        }

        // Create a session ID
        String timestamp = this.messageService.getNowTimestampString();
        String hashString = chapterLink + timestamp;
        String newSessionId = Hashing.sha256().hashString(hashString, StandardCharsets.UTF_8).toString();

        // Re-set the nickname if needed
        if (sessionNickname == null) {
            sessionNickname = newSessionId;
        }
        else if (sessionNickname.isBlank()) {
            sessionNickname = newSessionId;
        }

        // Create a session entry
        this.sessionService.addSession(newSessionId, sessionNickname, ArchiveParseType.CHAPTER, chapterLink);

        // Start the chapter parsing process
        CompletableFuture<ArchiveServerFutureData> newFuture = this.archiveIngestor.startCreateChapterTask(
                chapterLink, newSessionId
        );

        // Save the completable future reference
        this.sessionService.addToSessionMap(newSessionId, newFuture);

        // Return the response
        return new ArchiveServerResponseData(newSessionId, sessionNickname, this.messageService.getResponseNewChapterSession());
    }

    public ArchiveServerResponseData startParseStory(ArchiveServerRequestData request) {
        // Extract request items
        String storyLink = request.getPageLink();
        String sessionNickname = request.getSessionNickname();
        boolean nicknameSent = request.isNicknameSent();
        this.logService.createInfoLog(this.messageService.getLoggingInfoStoryObtainRequest());

        // Check if request passed inspection
        if (storyLink.isEmpty()) {
            return new ArchiveServerResponseData(this.messageService.getResponseBadURLFormat());
        }
        if (nicknameSent && sessionNickname.isEmpty()) {
            return new ArchiveServerResponseData(this.messageService.getResponseBadNicknameFormat());
        }

        // Create a session ID
        String timestamp = this.messageService.getNowTimestampString();
        String hashString = storyLink + timestamp;
        String newSessionId = Hashing.sha256().hashString(hashString, StandardCharsets.UTF_8).toString();

        // Re-set the nickname if needed
        if (sessionNickname == null) {
            sessionNickname = newSessionId;
        }
        else if (sessionNickname.isBlank()) {
            sessionNickname = newSessionId;
        }

        // Create a session entry
        this.sessionService.addSession(newSessionId, sessionNickname, ArchiveParseType.STORY, storyLink);

        // Start the story parsing process
        CompletableFuture<ArchiveServerFutureData> newFuture = this.archiveIngestor.startCreateStoryTask(
                storyLink, newSessionId
        );

        // Save the completable future reference
        this.sessionService.addToSessionMap(newSessionId, newFuture);

        // Return the response
        return new ArchiveServerResponseData(newSessionId, sessionNickname, this.messageService.getResponseNewStorySession());
    }

    public boolean validateSessionId(String sessionId) {
        Pattern sessionIdPattern = Pattern.compile("^[a-fA-F0-9]*$", Pattern.CASE_INSENSITIVE);
        return sessionIdPattern.matcher(sessionId).matches();
    }

    public ArchiveServerResponseData getSessionInformation(String sessionId) {
        // Validate the session ID
        boolean sessionIdValid = this.validateSessionId(sessionId);
        if (!sessionIdValid) {
            return new ArchiveServerResponseData(sessionId, this.messageService.getResponseBadSessionId());
        }

        // Get the session information or send a failed message
        ArchiveSession sessionEntity = this.sessionService.getSession(sessionId);
        if (sessionEntity != null) {
            ArchiveParse parseEntity = sessionEntity.getParseEntity();
            return new ArchiveServerResponseData(
                    sessionEntity.getId(), sessionEntity.getSessionNickname(), sessionEntity.isSessionFinished(),
                    sessionEntity.isSessionCanceled(), sessionEntity.isSessionException(),
                    parseEntity.getParseChaptersCompleted(), parseEntity.getParseChaptersTotal(),
                    parseEntity.getParseResult(), sessionEntity.getSessionLastMessage()
            );
        }
        else {
            return new ArchiveServerResponseData(sessionId, this.messageService.getResponseGetSessionFailed());
        }
    }

    public ArchiveServerResponseData cancelSession(String sessionId) {
        // Validate the session ID
        boolean sessionIdValid = this.validateSessionId(sessionId);
        if (!sessionIdValid) {
            return new ArchiveServerResponseData(sessionId, this.messageService.getResponseBadSessionId());
        }

        // Cancel the session's async task
        boolean success = this.sessionService.cancelSession(sessionId);
        if (success) {
            return new ArchiveServerResponseData(sessionId, this.messageService.getResponseCancelSucceeded());
        }
        else {
            return new ArchiveServerResponseData(sessionId, this.messageService.getResponseCancelFailed());
        }
    }
}
