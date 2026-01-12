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

/**
 * <p>This class is the Spring Boot service responsible for top-level actions used by the archive server controller.</p>
 */
@Service
public class ArchiveService {
    /**
     * <p>This attribute represents the injected {@link ArchiveIngestor}.</p>
     */
    @Autowired
    private final ArchiveIngestor archiveIngestor;

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

    /**
     * <p>This attribute represents the injected {@link ArchiveSessionService}.</p>
     */
    @Autowired
    private final ArchiveSessionService sessionService;

    /**
     * <p>This attribute represents the injected {@link ArchiveWebsocketService}.</p>
     */
    @Autowired
    private final ArchiveWebsocketService websocketService;

    /**
     * <p>This constructor injects services and values into the archive service.</p>
     * @param archiveIngestor The injected archive ingestor
     * @param logService The injected logging service.
     * @param messageService The injected message service.
     * @param sessionService The injected session service.
     * @param websocketService The injected websocket service.
     * @throws InterruptedException If the <code>Thread.sleep()</code> line in
     *      <code>sessionService.sessionTaskMonitor()</code> is interrupted mid-execution.
     */
    public ArchiveService(ArchiveIngestor archiveIngestor, ArchiveLogService logService,
                          ArchiveMessageService messageService, ArchiveSessionService sessionService,
                          ArchiveWebsocketService websocketService)
            throws InterruptedException {
        // Initialize components
        this.archiveIngestor = archiveIngestor;
        this.logService = logService;
        this.messageService = messageService;
        this.sessionService = sessionService;
        this.websocketService = websocketService;

        // Start task monitoring
        this.sessionService.sessionTaskMonitor();
    }

    /**
     * <p>This method returns archive server test data to the archive controller.</p>
     * @return An {@link ArchiveServerTestData} object.
     */
    public ArchiveServerTestData getArchiveIngestorTestData() {
        this.logService.createInfoLog(this.messageService.getLoggingInfoTestAPISend());
        return new ArchiveServerTestData(this.messageService.getTestDataValue());
    }

    /**
     * <p>This method returns archive server specification data to the archive controller.</p>
     * @return An {@link ArchiveServerSpecData} object.
     */
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

    /**
     * <p>
     *     This method starts a new parsing session and returns an archive server response confirming if starting the
     *     AO3 parsing was successful.
     * </p>
     * @param request The parse request formatted as a {@link ArchiveServerRequestData} object.
     * @param parseType A specific enum from {@link ArchiveParseType}.
     * @return An {@link ArchiveServerResponseData} object containing whether the parse request was successful.
     */
    public ArchiveServerResponseData startParse(ArchiveServerRequestData request, ArchiveParseType parseType) {
        // Extract request items
        String pageLink = request.getPageLink();
        String sessionNickname = request.getSessionNickname();
        if (parseType.equals(ArchiveParseType.CHAPTER)) {
            this.logService.createInfoLog(this.messageService.getLoggingInfoChapterObtainRequest());
        }
        else {
            this.logService.createInfoLog(this.messageService.getLoggingInfoStoryObtainRequest());
        }

        // Check if request passed inspection
        if (pageLink.isEmpty()) {
            return new ArchiveServerResponseData(this.messageService.getResponseBadURLFormat());
        }
        if (request.isNicknameSent() && sessionNickname.isEmpty()) {
            return new ArchiveServerResponseData(this.messageService.getResponseBadNicknameFormat());
        }

        // Check if a chapter-specific link was sent for story parsing
        if (pageLink.contains("chapters") && parseType.equals(ArchiveParseType.STORY)) {
            return new ArchiveServerResponseData(this.messageService.getResponseBadStoryLink());
        }

        // Create a session ID
        String timestamp = this.messageService.getNowTimestampString();
        String hashString = pageLink + timestamp;
        String newSessionId = Hashing.sha256().hashString(hashString, StandardCharsets.UTF_8).toString();

        // Re-set the nickname if needed
        if (sessionNickname == null) {
            sessionNickname = newSessionId;
        }
        else if (sessionNickname.isBlank()) {
            sessionNickname = newSessionId;
        }

        // Create a session entry
        this.sessionService.addSession(newSessionId, sessionNickname, parseType, pageLink);

        // Start the parsing process
        CompletableFuture<ArchiveServerFutureData> newFuture = this.archiveIngestor.startCreateTask(
                pageLink, newSessionId, parseType, request.getMaxCommentThreadDepth(),
                request.getMaxCommentPageLimit(), request.getMaxKudosPageLimit(), request.getMaxBookmarkPageLimit()
        );

        // Save the completable future reference
        this.sessionService.addToSessionMap(newSessionId, newFuture);

        // Return the response
        if (parseType.equals(ArchiveParseType.CHAPTER)) {
            return new ArchiveServerResponseData(
                    newSessionId, sessionNickname, this.messageService.getResponseNewChapterSession()
            );
        }
        else {
            return new ArchiveServerResponseData(
                    newSessionId, sessionNickname, this.messageService.getResponseNewStorySession()
            );
        }
    }

    /**
     * <p>This method validates if a session ID received from a client is formatted properly.</p>
     * @param sessionId The session ID received from the client.
     * @return A boolean value for whether the session ID is valid.
     */
    public boolean validateSessionId(String sessionId) {
        Pattern sessionIdPattern = Pattern.compile("^[a-fA-F0-9]*$", Pattern.CASE_INSENSITIVE);
        return sessionIdPattern.matcher(sessionId).matches();
    }

    /**
     * <p>This method returns a specific session's information to the archive controller.</p>
     * @param sessionId The session ID received from the client.
     * @return An {@link ArchiveServerResponseData} object containing the requested session information.
     */
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

    /**
     * <p>
     *     This method starts a live websocket feed for regular session updates and returns a feed start
     *     confirmation to the archive controller.
     * </p>
     * @param sessionId The session ID received from the client.
     * @return An {@link ArchiveServerResponseData} object containing if starting the live feed was successful.
     */
    public ArchiveServerResponseData getSessionInformationLive(String sessionId) throws InterruptedException {
        // Validate the session ID
        boolean sessionIdValid = this.validateSessionId(sessionId);
        if (!sessionIdValid) {
            return new ArchiveServerResponseData(sessionId, this.messageService.getResponseBadSessionId());
        }

        // Start sending through websockets
        String stompConfig = this.websocketService.getSTOMPConfig().toString();
        this.websocketService.runLiveSessionFeed(sessionId);

        // Return the response
        ArchiveServerResponseData r = new ArchiveServerResponseData(sessionId, this.messageService.getResponseNewSessionFeed());
        r.setParseResult(stompConfig);
        return r;
    }

    /**
     * <p>This method cancels an active session and returns a confirmation to the archive controller.</p>
     * @param sessionId The session ID received from the client.
     * @return An {@link ArchiveServerResponseData} object containing if canceling the session was successful.
     */
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
