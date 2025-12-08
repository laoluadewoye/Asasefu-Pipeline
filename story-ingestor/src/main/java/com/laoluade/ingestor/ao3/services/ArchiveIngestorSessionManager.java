package com.laoluade.ingestor.ao3.services;

// Server Classes
import com.laoluade.ingestor.ao3.models.ArchiveIngestorResponse;
import com.laoluade.ingestor.ao3.models.ArchiveIngestorTaskFuture;
import com.laoluade.ingestor.ao3.repositories.ArchiveIngestorSessionEntity;
import com.laoluade.ingestor.ao3.models.ArchiveIngestorSessionInfo;

// Logging and Spring Classes
import com.laoluade.ingestor.ao3.repositories.ArchiveIngestorSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

// Java Classes
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class ArchiveIngestorSessionManager {
    // Manager components
    @Autowired
    private final ArchiveIngestorLogManager logManager;

    @Autowired
    private final ArchiveIngestorMessageManager messageManager;

    // Repository
    @Autowired
    private ArchiveIngestorSessionRepository sessionRepository;

    // Attributes and constants
    private final Integer sessionPersistSecs;
    private final Integer checkIntervalMilli;

    public ArchiveIngestorSessionManager(@Value("${archiveIngestor.session.persistSecs:10}") Integer sessionPersistSecs,
                                         @Value("${archiveIngestor.session.checkIntervalMilli:2000}") Integer checkIntervalMilli,
                                         ArchiveIngestorLogManager logManager,
                                         ArchiveIngestorMessageManager messageManager) {
        // Set up component managers
        this.logManager = logManager;
        this.messageManager = messageManager;

        // Set up attributes
        this.sessionPersistSecs = sessionPersistSecs;
        this.checkIntervalMilli = checkIntervalMilli;

        // Start logger
        this.logManager.createInfoLog(this.messageManager.createSessionPersistSecsMessage(this.sessionPersistSecs));
        this.logManager.createInfoLog(this.messageManager.createSessionCheckIntervalMessage(this.checkIntervalMilli));
    }

    public synchronized void addSession(String newSessionID, ArchiveIngestorSessionEntity newSession) {
        this.sessionRepository.save(newSession);
        this.logManager.createInfoLog(this.messageManager.createAISMAddedSessionMessage(newSessionID));
    }

    public synchronized ArchiveIngestorSessionEntity getSession(String sessionID) {
        Optional<ArchiveIngestorSessionEntity> requestedSession = this.sessionRepository.findById(sessionID);
        if (requestedSession.isPresent()) {
            this.logManager.createInfoLog(this.messageManager.createAISMGetSessionMessage(sessionID));
            return requestedSession.get();
        }
        else {
            this.logManager.createInfoLog(this.messageManager.createAISMGetSessionFailedMessage(sessionID));
            return null;
        }
    }

    public synchronized void updateSession(String curSessionID, ArchiveIngestorResponse updatedResponse) {
        ArchiveIngestorSessionEntity requestedSession = this.getSession(curSessionID);
        if (requestedSession != null) {
            requestedSession.setSessionResponse(updatedResponse);
            this.sessionRepository.save(requestedSession);
            this.logManager.createInfoLog(this.messageManager.createAISMUpdatedSessionMessage(curSessionID));
        }
        else {
            this.logManager.createInfoLog(this.messageManager.createAISMGetSessionFailedMessage(curSessionID));
        }
    }

    public synchronized boolean cancelSession(String sessionID) {
        ArchiveIngestorSessionEntity requestedSession = this.getSession(sessionID);
        if (requestedSession != null) {
            CompletableFuture<ArchiveIngestorTaskFuture> sessionFuture = requestedSession.getSessionFuture();
            sessionFuture.cancel(true);
            this.logManager.createInfoLog(this.messageManager.createAISMCancelSessionMessage(sessionID));
            return true;
        }
        else {
            this.logManager.createInfoLog(this.messageManager.createAISMCancelSessionFailedMessage(sessionID));
            return false;
        }
    }

    public synchronized void purgeSessionMap() {
        // Get session IDs to delete
        ArrayList<String> sessionsToDelete = new ArrayList<>();
        ZonedDateTime currentTimestamp = ZonedDateTime.now(ZoneId.of(this.messageManager.getUTCValue()));

        for (ArchiveIngestorSessionEntity session : this.sessionRepository.findAll()) {
            ArchiveIngestorSessionInfo sessionInfo = session.getSessionResponse().getSessionInfo();
            ZonedDateTime lastSessionTimestamp = ZonedDateTime.parse(sessionInfo.getCreationTimestamp());
            ZonedDateTime lastValidSessionTime = lastSessionTimestamp.plusSeconds(this.sessionPersistSecs);

            if (currentTimestamp.isAfter(lastValidSessionTime)) {
                sessionsToDelete.add(session.getId());
            }
        }

        // Delete sessions by their session ID
        this.sessionRepository.deleteAllById(sessionsToDelete);
        this.logManager.createInfoLog(this.messageManager.createAISMDeleteSessionMessage(sessionsToDelete));
    }

    @Async("archiveIngestorAsyncExecutor")
    public void sessionValidityMonitor() throws InterruptedException {
        while (true) {
            // Wait a bit before checking
            Thread.sleep(Duration.ofMillis(checkIntervalMilli));

            // Conduct the check
            purgeSessionMap();
        }
    }
}
