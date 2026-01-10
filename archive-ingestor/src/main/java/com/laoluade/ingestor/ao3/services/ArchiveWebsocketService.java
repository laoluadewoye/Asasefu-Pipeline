package com.laoluade.ingestor.ao3.services;

import com.laoluade.ingestor.ao3.models.ArchiveServerResponseData;
import com.laoluade.ingestor.ao3.repositories.ArchiveParse;
import com.laoluade.ingestor.ao3.repositories.ArchiveSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * <p>This class is the Spring Boot service responsible for running live websocket feeds for active sessions.</p>
 * <p>This class uses the following settings from the application.properties file to configure itself:</p>
 * <ul>
 *     <li>archiveServer.websocket.sendIntervalMilli</li>
 * </ul>
 * <p>All <code>archiveServer.websocket</code> settings have a class attribute counterpart.</p>
 */
@Service
public class ArchiveWebsocketService {
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
     * <p>This attribute represents the {@link SimpMessagingTemplate} used to public websocket messages.</p>
     */
    @Autowired
    private final SimpMessagingTemplate websocketTemplate;

    /**
     * <p>This attribute represents the interval in milliseconds to wait before publishing a websocket message.</p>
     */
    private final Integer sendIntervalMilli;

    /**
     * <p>This constructor injects services and values into the archive websocket service.</p>
     * @param logService The injected logging service.
     * @param messageService The injected message service.
     * @param sessionService The injected session service.
     * @param websocketTemplate The injected {@link SimpMessagingTemplate}.
     * @param sendIntervalMilli Size of message publishing interval in seconds.
     */
    public ArchiveWebsocketService(ArchiveLogService logService, ArchiveMessageService messageService,
                                   ArchiveSessionService sessionService, SimpMessagingTemplate websocketTemplate,
                                   @Value("${archiveServer.websocket.sendIntervalMilli}") Integer sendIntervalMilli) {
        this.logService = logService;
        this.messageService = messageService;
        this.sessionService = sessionService;
        this.websocketTemplate = websocketTemplate;
        this.sendIntervalMilli = sendIntervalMilli;
    }

    /**
     * <p>This method runs an asynchronous task that publishes session updates in intervals of milliseconds.</p>
     * @param sessionId The session ID to routinely check for updates.
     * @throws InterruptedException If the <code>Thread.sleep()</code> line is interrupted mid-execution.
     */
    @Async("archiveServerAsyncExecutor")
    public void runLiveSessionFeed(String sessionId) throws InterruptedException {
        boolean sessionStillLive;
        do {
            // Sleep for a set time
            Thread.sleep(this.sendIntervalMilli);

            // Get the session information or send a failed message
            ArchiveSession sessionEntity = this.sessionService.getSession(sessionId);
            ArchiveServerResponseData responseData;

            if (sessionEntity != null) {
                ArchiveParse parseEntity = sessionEntity.getParseEntity();
                responseData = new ArchiveServerResponseData(
                        sessionEntity.getId(), sessionEntity.getSessionNickname(), sessionEntity.isSessionFinished(),
                        sessionEntity.isSessionCanceled(), sessionEntity.isSessionException(),
                        parseEntity.getParseChaptersCompleted(), parseEntity.getParseChaptersTotal(),
                        parseEntity.getParseResult(), sessionEntity.getSessionLastMessage()
                );
            }
            else {
                responseData = new ArchiveServerResponseData(sessionId, this.messageService.getResponseGetSessionFailed());
            }

            // Send response data
            this.websocketTemplate.convertAndSend("/api/v1/websocket/topic/get-session-live", responseData);

            // Log it
            this.logService.createInfoLog(this.messageService.createWSSentMessage(sessionId, responseData.getResponseMessage()));

            // Run a check to decide if to continue
            boolean messageIsFail = responseData.getResponseMessage().equals(
                    this.messageService.getResponseGetSessionFailed()
            );
            boolean sessionComplete = responseData.isSessionFinished() || responseData.isSessionCanceled() ||
                    responseData.isSessionException();

            sessionStillLive = !messageIsFail && !sessionComplete;
        } while (sessionStillLive);
    }
}
