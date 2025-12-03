package com.laoluade.ingestor.ao3.server.services;

// Local classes
import com.laoluade.ingestor.ao3.core.ArchiveIngestor;
import com.laoluade.ingestor.ao3.server.models.*;
import com.laoluade.ingestor.ao3.server.tasks.ArchiveIngestorAsyncTasks;

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
    // Async task bean
    @Autowired
    private ArchiveIngestorAsyncTasks archiveIngestorAsyncTasks;

    // Attributes
    private final String driverSocket;
    private final Integer sessionPersistSecs;
    private final HashMap<String, ArchiveIngestorSession> sessionManager;

    // Logging
    private final Logger archiveIngestorServiceLogger;

    public ArchiveIngestorService(@Value("${archiveIngestor.driver.socket}") String driverSocket,
                                  @Value("${archiveIngestor.session.persistSecs:120}") Integer sessionPersistSecs) {
        this.driverSocket = driverSocket;
        this.sessionPersistSecs = sessionPersistSecs;
        this.sessionManager = new HashMap<String, ArchiveIngestorSession>();
        this.archiveIngestorServiceLogger = LoggerFactory.getLogger(ArchiveIngestorService.class);
        this.archiveIngestorServiceLogger.info("Driver socket is set to {}.", this.driverSocket);
        this.archiveIngestorServiceLogger.info("Session Persistence is set to {} seconds.", this.sessionPersistSecs);
    }

    public ArchiveIngestorTestAPIInfo getArchiveIngestorTestAPI() {
        this.archiveIngestorServiceLogger.info("Sending test API string.");
        String info = "Hello Archive Ingestor Version 1 API!";
        return new ArchiveIngestorTestAPIInfo(info);
    }

    public ArchiveIngestorInfo getArchiveIngestorInfo() {
        try {
            JSONArray supportedOTWArchiveVersions = new ArchiveIngestor().versionTable.getJSONArray(ArchiveIngestor.VERSION);
            String lastSupportedOTWArchiveVersion = supportedOTWArchiveVersions.toList().getLast().toString();
            this.archiveIngestorServiceLogger.info(
                    "Sending version number {} and OTW Archive version {}.",
                    ArchiveIngestor.VERSION, lastSupportedOTWArchiveVersion
            );

            return new ArchiveIngestorInfo(ArchiveIngestor.VERSION, lastSupportedOTWArchiveVersion);
        }
        catch (IOException e) {
            return new ArchiveIngestorInfo("[Failed IO]", "[Failed IO]");
        }
        catch (Exception e) {
            return new ArchiveIngestorInfo("[Failed Unknown]", "[Failed Unknown]");
        }
    }

    public ArchiveIngestorResponse startParseChapter(ArchiveIngestorRequest request) {
        // Extract request items
        URL chapterURL = request.getPageLinkURL();
        String sessionNickname = request.getSessionNickname();
        this.archiveIngestorServiceLogger.info("Successfully obtained chapter parsing request contents.");

        // Create a session ID
        String timestamp = ZonedDateTime.now(ZoneId.of("UTC")).toString();
        String hashString = chapterURL.toString() + timestamp;
        String newSessionID = Hashing.sha256().hashString(hashString, StandardCharsets.UTF_8).toString();

        // Create response object and start filling it in
        ArchiveIngestorSessionInfo newSessionInfo = new ArchiveIngestorSessionInfo(newSessionID, sessionNickname);
        ArchiveIngestorResponse newResponse = new ArchiveIngestorResponse(
                "", "New chapter parsing session created.", newSessionInfo
        );

        // Start the chapter parsing process
        CompletableFuture<ArchiveIngestorTaskFuture> newFuture = this.archiveIngestorAsyncTasks.parseChapter(
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
        this.archiveIngestorServiceLogger.info("Successfully obtained story parsing request contents.");

        // Create a session ID
        String timestamp = ZonedDateTime.now(ZoneId.of("UTC")).toString();
        String hashString = storyURL.toString() + timestamp;
        String newSessionID = Hashing.sha256().hashString(hashString, StandardCharsets.UTF_8).toString();

        // Create response object and start filling it in
        ArchiveIngestorSessionInfo newSessionInfo = new ArchiveIngestorSessionInfo(newSessionID, sessionNickname);
        ArchiveIngestorResponse newResponse = new ArchiveIngestorResponse(
                "", "New story parsing session created.", newSessionInfo
        );

        // Start the story parsing process
        CompletableFuture<ArchiveIngestorTaskFuture> newFuture = this.archiveIngestorAsyncTasks.parseStory(
                this.driverSocket, this.archiveIngestorServiceLogger, storyURL, newResponse
        );

        // Add session to session manager
        this.sessionManager.put(newSessionID, new ArchiveIngestorSession(newResponse, newFuture));

        // Return the response
        return newResponse;
    }

    public void purgeSessionManager() {
        ArrayList<String> sessionsToDelete = new ArrayList<>();
        ZonedDateTime currentTimestamp = ZonedDateTime.now(ZoneId.of("UTC"));

        // Get session IDs to delete
        for (HashMap.Entry<String, ArchiveIngestorSession> sessionEntry : this.sessionManager.entrySet()) {
            ZonedDateTime lastSessionTimestamp = sessionEntry.getValue().getSessionResponse().getSessionInfo().getCreationTimestamp();
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
            return new ArchiveIngestorResponse("", "Session either doesn't exist or has been deleted.");
        }
    }

    public ArchiveIngestorResponse cancelSession(String sessionID) {
        // Purge the session manager
        purgeSessionManager();

        // Cancel the session's async task
        this.sessionManager.get(sessionID).getSessionFuture().cancel(true);

        // Return a notification
        return new ArchiveIngestorResponse("", "Sent cancel signal to the task.");
    }
}
