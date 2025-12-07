package com.laoluade.ingestor.ao3.services;

// Server Classes
import com.laoluade.ingestor.ao3.models.ArchiveIngestorResponse;
import com.laoluade.ingestor.ao3.models.ArchiveIngestorSession;
import com.laoluade.ingestor.ao3.models.ArchiveIngestorSessionInfo;

// Logging and Spring Classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

// Java Classes
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;

// TODO: Consider turning this into a JDBA repository
@Component
public class ArchiveIngestorSessionManager {
    // Manager components
    @Autowired
    private final ArchiveIngestorLogManager logManager;

    @Autowired
    private final ArchiveIngestorMessageManager messageManager;

    // Attributes and constants
    private final Integer sessionPersistSecs;
    private final Integer checkIntervalMilli;
    private final HashMap<String, ArchiveIngestorSession> sessionMap;

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
        this.sessionMap = new HashMap<String, ArchiveIngestorSession>();

        // Start logger
        this.logManager.createInfoLog(this.messageManager.createSessionPersistSecsMessage(this.sessionPersistSecs));
        this.logManager.createInfoLog(this.messageManager.createSessionCheckIntervalMessage(this.checkIntervalMilli));
    }

    public synchronized void addSession(String newSessionID, ArchiveIngestorSession newSession) {
        this.sessionMap.put(newSessionID, newSession);
        this.logManager.createInfoLog(this.messageManager.createAISMAddedSessionMessage(newSessionID));
    }

    public synchronized ArchiveIngestorSession getSession(String sessionID) {
        ArchiveIngestorSession requestedSession = this.sessionMap.get(sessionID);
        if (requestedSession != null) {
            this.logManager.createInfoLog(this.messageManager.createAISMGetSessionMessage(sessionID));
        }
        else {
            this.logManager.createInfoLog(this.messageManager.createAISMGetSessionFailedMessage(sessionID));
        }
        return requestedSession;
    }

    public synchronized void updateSession(String curSessionID, ArchiveIngestorResponse updatedResponse) {
        ArchiveIngestorSession session = this.getSession(curSessionID);
        session.setSessionResponse(updatedResponse);

        this.logManager.createInfoLog(this.messageManager.createAISMUpdatedSessionMessage(curSessionID));
    }

    public synchronized boolean cancelSession(String sessionID) {
        ArchiveIngestorSession session = this.getSession(sessionID);
        if (session != null) {
            session.getSessionFuture().cancel(true);
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

        for (HashMap.Entry<String, ArchiveIngestorSession> sessionEntry : this.sessionMap.entrySet()) {
            ArchiveIngestorSessionInfo sessionInfo = sessionEntry.getValue().getSessionResponse().getSessionInfo();
            ZonedDateTime lastSessionTimestamp = ZonedDateTime.parse(sessionInfo.getCreationTimestamp());
            ZonedDateTime lastValidSessionTime = lastSessionTimestamp.plusSeconds(this.sessionPersistSecs);

            if (currentTimestamp.isAfter(lastValidSessionTime)) {
                sessionsToDelete.add(sessionEntry.getKey());
            }
        }

        // Delete sessions by their session ID
        for (String sessionID : sessionsToDelete) {
            this.sessionMap.remove(sessionID);
            this.logManager.createInfoLog(this.messageManager.createAISMDeleteSessionMessage(sessionID));
        }
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
