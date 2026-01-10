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

/**
 * <p>This class is the Spring Boot service responsible for managing interacting with active and completed sessions.</p>
 * <p>This class uses the following settings from the application.properties file to configure itself:</p>
 * <ul>
 *     <li>archiveServer.session.persistSecs</li>
 *     <li>archiveServer.session.checkIntervalMilli</li>
 * </ul>
 * <p>All <code>archiveServer.session</code> settings have a class attribute counterpart.</p>
 */
@Service
public class ArchiveSessionService {
    // Service components
    /**
     * <p>This attribute represents the injected {@link ArchiveLogService}.</p>
     */
    @Autowired
    private final ArchiveLogService logService;

    /**
     * <p>This attribute represents the injected {@link ArchiveMessageService}.</p>
     */
    @Autowired
    private final ArchiveMessageService messageService;

    // Repository components
    /**
     * <p>This attribute represents the {@link ArchiveSessionRepository} interface.</p>
     */
    @Autowired
    private ArchiveSessionRepository sessionRepository;

    /**
     * <p>This attribute represents the {@link ArchiveParseRepository} interface.</p>
     */
    @Autowired
    private ArchiveParseRepository parseRepository;

    // Attributes and constants
    /**
     * <p>This attribute represents the in-memory map used for tracking active sessions.</p>
     */
    private final HashMap<String, CompletableFuture<ArchiveServerFutureData>> sessionMap;

    /**
     * <p>This attribute represents the amount of time sessions can exist in active tracking without having an update.</p>
     */
    private final Integer sessionPersistSecs;

    /**
     * <p>
     *     This attribute represents the interval in milliseconds that the session service waits before checking
     *     session activity.
     * </p>
     */
    private final Integer checkIntervalMilli;

    /**
     * <p>This constructor injects services and values into the archive session service.</p>
     * @param sessionPersistSecs Amount of persistence time in seconds.
     * @param checkIntervalMilli Session checking interval in milliseconds.
     * @param logService The injected logging service.
     * @param messageService The injected message service.
     */
    public ArchiveSessionService(@Value("${archiveServer.session.persistSecs}") Integer sessionPersistSecs,
                                 @Value("${archiveServer.session.checkIntervalMilli}") Integer checkIntervalMilli,
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

    /**
     * <p>This method adds a new session to the database.</p>
     * @param newSessionId The session ID.
     * @param sessionNickname The session's nickname.
     * @param parseType The type of parsing running inside the session.
     * @param parseTargetLink The target web link of the parsing.
     */
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

        this.logService.createInfoLog(this.messageService.createASSAddedSessionMessage(newSessionId));
    }

    /**
     * <p>This method adds a new session to the session tracking map.</p>
     * @param newSessionId The session ID.
     * @param newFuture The session's {@link CompletableFuture}.
     */
    public synchronized void addToSessionMap(String newSessionId, CompletableFuture<ArchiveServerFutureData> newFuture) {
        this.sessionMap.put(newSessionId, newFuture);
        this.logService.createInfoLog(this.messageService.createASSAddedSessionMapMessage(newSessionId));
    }

    /**
     * <p>This method retrieves a session entry from the session table using the session ID.</p>
     * @param sessionId The session ID.
     * @return The corresponding {@link ArchiveSession} entity.
     */
    public synchronized ArchiveSession getSession(String sessionId) {
        Optional<ArchiveSession> requestedSessionEntity = this.sessionRepository.findById(sessionId);
        if (requestedSessionEntity.isPresent()) {
            this.logService.createInfoLog(this.messageService.createASSGetSessionMessage(sessionId));
            return requestedSessionEntity.get();
        }
        else {
            this.logService.createInfoLog(this.messageService.createASSGetSessionFailedMessage(sessionId, "Get Full"));
            return null;
        }
    }

    /**
     * <p>This method retrieves the cancel status of a session entry using the session ID.</p>
     * @param sessionId The session ID.
     * @return The corresponding session's canceled flag.
     */
    public synchronized boolean getCanceledStatus(String sessionId) {
        ArchiveSession sessionEntity = this.getSession(sessionId);
        if (sessionEntity != null) {
            this.logService.createInfoLog(this.messageService.createASSGetSessionCancelMessage(sessionId));
            return sessionEntity.isSessionCanceled();
        }
        else {
            this.logService.createInfoLog(this.messageService.createASSGetSessionFailedMessage(sessionId, "Get Canceled Status"));
            return false;
        }
    }

    /**
     * <p>This method cancels a session using the session ID.</p>
     * @param sessionId The session ID.
     * @return A boolean for whether the operation was successful.
     */
    @Transactional
    public synchronized boolean cancelSession(String sessionId) {
        ArchiveSession requestedSessionEntity = this.getSession(sessionId);
        if (requestedSessionEntity != null) {
            this.sessionRepository.updateCanceledStatus(sessionId);
            this.logService.createInfoLog(this.messageService.createASSCancelSessionMessage(sessionId));
            return true;
        }
        else {
            this.logService.createInfoLog(this.messageService.createASSGetSessionFailedMessage(sessionId, "Cancel"));
            this.logService.createInfoLog(this.messageService.createASSCancelSessionFailedMessage(sessionId));
            return false;
        }
    }

    /**
     * <p>This method updates a session entry with full session entry.</p>
     * @param sessionId The session ID.
     * @param updatedSessionEntity The new {@link ArchiveSession} entity to replace the old one.
     */
    @Transactional
    public synchronized void updateSessionFull(String sessionId, ArchiveSession updatedSessionEntity) {
        ArchiveSession oldSessionEntity = this.getSession(sessionId);
        if (oldSessionEntity != null) {
            this.sessionRepository.save(updatedSessionEntity);
            this.logService.createInfoLog(this.messageService.createASSUpdatedSessionFullMessage(sessionId));
        }
        else {
            this.logService.createInfoLog(this.messageService.createASSGetSessionFailedMessage(sessionId, "Full Update"));
        }
    }

    /**
     * <p>This method updates a session entry's last recorded message.</p>
     * @param sessionId The session ID.
     * @param newLastRecordedMessage The new message to replace the old one.
     */
    @Transactional
    public synchronized void updateLastRecordedMessage(String sessionId, String newLastRecordedMessage) {
        ArchiveSession sessionEntity = this.getSession(sessionId);
        if (sessionEntity != null) {
            // Update the total chapter count
            this.sessionRepository.updateLastMessage(sessionId, newLastRecordedMessage);

            // Update the session updated timestamp
            this.sessionRepository.updateSessionUpdatedTimestamp(sessionId, this.messageService.getNowTimestampString());

            this.logService.createInfoLog(this.messageService.createASSUpdateLastRecordedMessage(sessionId, newLastRecordedMessage));
        }
        else {
            this.logService.createInfoLog(this.messageService.createASSGetSessionFailedMessage(sessionId, "Last Message Update"));
        }
    }

    /**
     * <p>This method updates a session entry's total chapter count.</p>
     * @param sessionId The session ID.
     * @param chapterCount The new chapter count to replace the old one.
     */
    @Transactional
    public synchronized void updateChaptersTotal(String sessionId, Integer chapterCount) {
        ArchiveSession sessionEntity = this.getSession(sessionId);
        if (sessionEntity != null) {
            // Update the total chapter count
            this.parseRepository.updateChaptersTotal(sessionEntity.getParseEntity().getId(), chapterCount);

            // Update the session updated timestamp
            this.sessionRepository.updateSessionUpdatedTimestamp(sessionId, this.messageService.getNowTimestampString());

            this.logService.createInfoLog(this.messageService.createASSUpdateChaptersTotal(sessionId, chapterCount));
        }
        else {
            this.logService.createInfoLog(this.messageService.createASSGetSessionFailedMessage(sessionId, "Chapter Total Update"));
        }
    }

    /**
     * <p>This method updates a session entry's completed chapter count.</p>
     * @param sessionId The session ID.
     * @param chapterCount The new chapter count to replace the old one.
     */
    @Transactional
    public synchronized void updateChaptersCompleted(String sessionId, Integer chapterCount) {
        ArchiveSession sessionEntity = this.getSession(sessionId);
        if (sessionEntity != null) {
            // Update the total chapter count
            this.parseRepository.updateChaptersCompleted(sessionEntity.getParseEntity().getId(), chapterCount);

            // Update the session updated timestamp
            this.sessionRepository.updateSessionUpdatedTimestamp(sessionId, this.messageService.getNowTimestampString());

            this.logService.createInfoLog(this.messageService.createASSUpdateChaptersCompleted(sessionId, chapterCount));
        }
        else {
            this.logService.createInfoLog(this.messageService.createASSGetSessionFailedMessage(sessionId, "Chapter Completed Update"));
        }
    }

    /**
     * <p>
     *     This method updates the purge status of a list of sessions. This method also updates the last recorded
     *     message of the list of sessions since asynchronous tasks can update itself even after the session is
     *     officially declared done.
     * </p>
     * @param sessionsToDelete The list of session IDs.
     * @param finalSessionMessages The list of corresponding last messages.
     */
    @Transactional
    public synchronized void updatePurgeStatus(ArrayList<String> sessionsToDelete,
                                               ArrayList<String> finalSessionMessages) {
        for (int i = 0; i < sessionsToDelete.size(); i++) {
            this.sessionRepository.updatePurgedStatus(sessionsToDelete.get(i));
            this.sessionRepository.updateLastMessage(sessionsToDelete.get(i), finalSessionMessages.get(i));
        }
    }

    /**
     * <p>This method checks the session tracking map for sessions that have not been recently updated and removes them.</p>
     */
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
            this.logService.createInfoLog(this.messageService.createASSPurgeCheckMessage(curSessionId));
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
                this.logService.createErrorLog(this.messageService.createASSPurgeAssertFailed(curSessionId));
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
            this.logService.createInfoLog(this.messageService.createASSPurgeSessionMessage(sessionsToDelete));
        }
    }

    // TODO: Figure out a way to force async threads to stop when the server stops
    /**
     * <p>
     *     This method is an asynchronous session task monitor that runs the <code>purgeOldTasks()</code> method
     *     in intervals.
     * </p>
     * @throws InterruptedException If the <code>Thread.sleep()</code> line is interrupted mid-execution.
     */
    @Async("archiveServerAsyncExecutor")
    public void sessionTaskMonitor() throws InterruptedException {
        while (true) {
            // Wait a bit before checking
            Thread.sleep(Duration.ofMillis(checkIntervalMilli));

            // Conduct the check
            purgeOldTasks();
        }
    }
}
