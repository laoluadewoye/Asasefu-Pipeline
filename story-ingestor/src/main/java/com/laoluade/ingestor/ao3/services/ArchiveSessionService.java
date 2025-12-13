package com.laoluade.ingestor.ao3.services;

// Server Classes
import com.laoluade.ingestor.ao3.models.ArchiveServerFutureData;
import com.laoluade.ingestor.ao3.repositories.*;

// Spring Classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Java Classes
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ArchiveSessionService {
    // Service components
    @Autowired
    private final ArchiveLogService logService;

    @Autowired
    private final ArchiveMessageService messageService;

    // Repository components
    @Autowired
    private ArchiveSessionRepository sessionRepository;

    @Autowired
    private ArchiveParseRepository parseRepository;

    // Attributes and constants
    private final HashMap<String, CompletableFuture<ArchiveServerFutureData>> sessionMap;
    private final Integer sessionPersistSecs;
    private final Integer checkIntervalMilli;

    public ArchiveSessionService(@Value("${archiveIngestor.session.persistSecs:10}") Integer sessionPersistSecs,
                                 @Value("${archiveIngestor.session.checkIntervalMilli:2000}") Integer checkIntervalMilli,
                                 ArchiveLogService logService,
                                 ArchiveMessageService messageService) {
        // Set up component managers
        this.logService = logService;
        this.messageService = messageService;

        // Set up attributes
        this.sessionMap = new HashMap<>();
        this.sessionPersistSecs = sessionPersistSecs;
        this.checkIntervalMilli = checkIntervalMilli;

        // Start logger
        this.logService.createInfoLog(this.messageService.createSessionPersistSecsMessage(this.sessionPersistSecs));
        this.logService.createInfoLog(this.messageService.createSessionCheckIntervalMessage(this.checkIntervalMilli));
    }

    @Transactional
    public synchronized void addSession(String newSessionId, String sessionNickname, ArchiveParseType parseType,
                                        String parseTargetLink) {
        // Create a parse entry
        ArchiveParse newParseEntity = new ArchiveParse(
                parseType, parseTargetLink, 0, 0, ""
        );

        // Create session timestamp
        String creationTimestamp = this.messageService.getNowTimestampString();

        // Create and save a session entry
        this.sessionRepository.save(new ArchiveSession(
                newSessionId, sessionNickname, creationTimestamp, creationTimestamp,
                false, false, false, false,
                this.messageService.getDefaultRecordedMessage(), newParseEntity
        ));

        this.logService.createInfoLog(this.messageService.createASMAddedSessionMessage(newSessionId));
    }

    public synchronized void addToSessionMap(String newSessionId, CompletableFuture<ArchiveServerFutureData> newFuture) {
        this.sessionMap.put(newSessionId, newFuture);
        this.logService.createInfoLog(this.messageService.createASMAddedSessionMapMessage(newSessionId));
    }

    public synchronized ArchiveSession getSession(String sessionId) {
        Optional<ArchiveSession> requestedSessionEntity = this.sessionRepository.findById(sessionId);
        if (requestedSessionEntity.isPresent()) {
            this.logService.createInfoLog(this.messageService.createASMGetSessionMessage(sessionId));
            return requestedSessionEntity.get();
        }
        else {
            this.logService.createInfoLog(this.messageService.createASMGetSessionFailedMessage(sessionId, "Get Full"));
            return null;
        }
    }

    public synchronized boolean getCanceledStatus(String sessionId) {
        ArchiveSession sessionEntity = this.getSession(sessionId);
        if (sessionEntity != null) {
            this.logService.createInfoLog(this.messageService.createASMGetSessionCancelMessage(sessionId));
            return sessionEntity.isSessionCanceled();
        }
        else {
            this.logService.createInfoLog(this.messageService.createASMGetSessionFailedMessage(sessionId, "Get Canceled Status"));
            return false;
        }
    }

    @Transactional
    public synchronized boolean cancelSession(String sessionId) {
        ArchiveSession requestedSessionEntity = this.getSession(sessionId);
        if (requestedSessionEntity != null) {
            this.sessionRepository.updateCanceledStatus(sessionId);
            this.logService.createInfoLog(this.messageService.createASMCancelSessionMessage(sessionId));
            return true;
        }
        else {
            this.logService.createInfoLog(this.messageService.createASMGetSessionFailedMessage(sessionId, "Cancel"));
            this.logService.createInfoLog(this.messageService.createASMCancelSessionFailedMessage(sessionId));
            return false;
        }
    }

    @Transactional
    public synchronized void updateSessionFull(String sessionId, ArchiveSession updatedSessionEntity) {
        ArchiveSession oldSessionEntity = this.getSession(sessionId);
        if (oldSessionEntity != null) {
            this.sessionRepository.save(updatedSessionEntity);
            this.logService.createInfoLog(this.messageService.createASMUpdatedSessionFullMessage(sessionId));
        }
        else {
            this.logService.createInfoLog(this.messageService.createASMGetSessionFailedMessage(sessionId, "Full Update"));
        }
    }

    @Transactional
    public synchronized void updateLastRecordedMessage(String sessionId, String newLastRecordedMessage) {
        ArchiveSession sessionEntity = this.getSession(sessionId);
        if (sessionEntity != null) {
            // Update the total chapter count
            this.sessionRepository.updateLastMessage(sessionId, newLastRecordedMessage);

            // Update the session updated timestamp
            this.sessionRepository.updateSessionUpdatedTimestamp(sessionId, this.messageService.getNowTimestampString());

            this.logService.createInfoLog(this.messageService.createASMUpdateLastRecordedMessage(sessionId, newLastRecordedMessage));
        }
        else {
            this.logService.createInfoLog(this.messageService.createASMGetSessionFailedMessage(sessionId, "Last Message Update"));
        }
    }

    @Transactional
    public synchronized void updateChaptersTotal(String sessionId, Integer chapterCount) {
        ArchiveSession sessionEntity = this.getSession(sessionId);
        if (sessionEntity != null) {
            // Update the total chapter count
            this.parseRepository.updateChaptersTotal(sessionEntity.getParseEntity().getId(), chapterCount);

            // Update the session updated timestamp
            this.sessionRepository.updateSessionUpdatedTimestamp(sessionId, this.messageService.getNowTimestampString());

            this.logService.createInfoLog(this.messageService.createASMUpdateChaptersTotal(sessionId, chapterCount));
        }
        else {
            this.logService.createInfoLog(this.messageService.createASMGetSessionFailedMessage(sessionId, "Chapter Total Update"));
        }
    }

    @Transactional
    public synchronized void updateChaptersCompleted(String sessionId, Integer chapterCount) {
        ArchiveSession sessionEntity = this.getSession(sessionId);
        if (sessionEntity != null) {
            // Update the total chapter count
            this.parseRepository.updateChaptersCompleted(sessionEntity.getParseEntity().getId(), chapterCount);

            // Update the session updated timestamp
            this.sessionRepository.updateSessionUpdatedTimestamp(sessionId, this.messageService.getNowTimestampString());

            this.logService.createInfoLog(this.messageService.createASMUpdateChaptersCompleted(sessionId, chapterCount));
        }
        else {
            this.logService.createInfoLog(this.messageService.createASMGetSessionFailedMessage(sessionId, "Chapter Completed Update"));
        }
    }

    @Transactional
    public synchronized void updatePurgeStatus(ArrayList<String> sessionsToDelete,
                                               ArrayList<String> finalSessionMessages) {
        for (int i = 0; i < sessionsToDelete.size(); i++) {
            this.sessionRepository.updatePurgedStatus(sessionsToDelete.get(i));
            this.sessionRepository.updateLastMessage(sessionsToDelete.get(i), finalSessionMessages.get(i));
        }
    }

    public synchronized void purgeOldTasks() {
        // TODO: Note that currentTimestamp probably has a millisecond long range of error where it could
        //      delete something it's not supposed to.
        // Get session IDs to delete
        ArrayList<String> sessionsToDelete = new ArrayList<>();
        ArrayList<String> finalSessionMessages = new ArrayList<>();
        ZonedDateTime currentTimestamp = this.messageService.getNowTimestamp();

        for (Map.Entry<String, CompletableFuture<ArchiveServerFutureData>> sessionContentEntry : this.sessionMap.entrySet()) {
            // Obtain session entity
            String curSessionId = sessionContentEntry.getKey();
            this.logService.createInfoLog(this.messageService.createASMPurgeCheckMessage(curSessionId));
            ArchiveSession curSessionEntity = this.getSession(curSessionId);

            if (curSessionEntity != null) {
                ZonedDateTime lastSessionUpdated = ZonedDateTime.parse(curSessionEntity.getSessionUpdated());
                ZonedDateTime lastValidSessionTime = lastSessionUpdated.plusSeconds(this.sessionPersistSecs);

                if (currentTimestamp.isAfter(lastValidSessionTime)) {
                    // Add id
                    sessionsToDelete.add(curSessionId);

                    // Add message
                    CompletableFuture<ArchiveServerFutureData> curFuture = this.sessionMap.get(curSessionId);
                    if (curFuture.isDone()) {
                        try {
                            finalSessionMessages.add(curFuture.get().getResultMessage());
                        }
                        catch (CancellationException | ExecutionException | InterruptedException e) {
                            finalSessionMessages.add(curSessionEntity.getSessionLastMessage());
                        }
                    }
                    else {
                        finalSessionMessages.add(curSessionEntity.getSessionLastMessage());
                    }
                }
            }
            else {
                this.logService.createErrorLog(this.messageService.createASMPurgeAssertFailed(curSessionId));
            }
        }

        // Update purge status in database
        this.updatePurgeStatus(sessionsToDelete, finalSessionMessages);

        // Canceling and purging sessions by their session ID
        for (String sessionId : sessionsToDelete) {
            this.sessionMap.get(sessionId).cancel(true);
            this.sessionMap.remove(sessionId);
        }

        if (!sessionsToDelete.isEmpty()) {
            this.logService.createInfoLog(this.messageService.createASMPurgeSessionMessage(sessionsToDelete));
        }
    }

    // TODO: Figure out a way to force async threads to stop when the server stops
    @Async("archiveIngestorAsyncExecutor")
    public void sessionTaskMonitor() throws InterruptedException {
        while (true) {
            // Wait a bit before checking
            Thread.sleep(Duration.ofMillis(checkIntervalMilli));

            // Conduct the check
            purgeOldTasks();
        }
    }
}
