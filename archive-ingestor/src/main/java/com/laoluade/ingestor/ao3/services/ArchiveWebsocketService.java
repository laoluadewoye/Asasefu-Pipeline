package com.laoluade.ingestor.ao3.services;

import com.laoluade.ingestor.ao3.models.ArchiveServerResponseData;
import com.laoluade.ingestor.ao3.repositories.ArchiveParse;
import com.laoluade.ingestor.ao3.repositories.ArchiveSession;
import org.json.JSONObject;
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
 *     <li>archiveServer.websocket.endpointURL</li>
 *     <li>archiveServer.websocket.topicURL</li>
 *     <li>archiveServer.websocket.appURL</li>
 *     <li>server.port</li>
 * </ul>
 * <p>All class attributes correspond to their <code>archiveServer.websocket</code> and <code>server</code> counterparts.</p>
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
     * <p>This attribute represents the relative URL of the websocket registry.</p>
     */
    private final String endpointURL;

    /**
     * <p>This attribute represents the relative URL of websocket topics.</p>
     */
    private final String topicURL;

    /**
     * <p>This attribute represents the relative URL of websocket APIs.</p>
     */
    private final String appURL;

    /**
     * <p>This attribute represents the port number the websocket service is sitting on.</p>
     */
    private final Integer port;

    /**
     * <p>This constructor injects services and values into the archive websocket service.</p>
     * @param logService The injected logging service.
     * @param messageService The injected message service.
     * @param sessionService The injected session service.
     * @param websocketTemplate The injected {@link SimpMessagingTemplate}.
     * @param sendIntervalMilli Size of message publishing interval in seconds.
     * @param endpointURL The relative websocket URL for subscribing.
     * @param topicURL The relative websocket URL for topics.
     * @param appURL The relative websocket URL for APIs.
     * @param port The port the websocket is bound to.
     */
    public ArchiveWebsocketService(ArchiveLogService logService, ArchiveMessageService messageService,
                                   ArchiveSessionService sessionService, SimpMessagingTemplate websocketTemplate,
                                   @Value("${archiveServer.websocket.sendIntervalMilli}") Integer sendIntervalMilli,
                                   @Value("${archiveServer.websocket.endpointURL}") String endpointURL,
                                   @Value("${archiveServer.websocket.topicURL}") String topicURL,
                                   @Value("${archiveServer.websocket.appURL}") String appURL,
                                   @Value("${server.port}") Integer port) {
        // Inject services and template
        this.logService = logService;
        this.messageService = messageService;
        this.sessionService = sessionService;
        this.websocketTemplate = websocketTemplate;

        // Setup configuration attributes
        this.sendIntervalMilli = sendIntervalMilli;
        this.logService.createInfoLog(this.messageService.createWebsocketSendIntervalMessage(this.sendIntervalMilli));

        this.endpointURL = endpointURL;
        this.logService.createInfoLog(this.messageService.createWebsocketEndpointMessage(this.endpointURL));

        this.topicURL = topicURL;
        this.logService.createInfoLog(this.messageService.createWebsocketTopicMessage(this.topicURL));

        this.appURL = appURL;
        this.logService.createInfoLog(this.messageService.createWebsocketAppMessage(this.appURL));

        this.port = port;
        this.logService.createInfoLog(this.messageService.createWebsocketPortMessage(this.port));
    }

    /**
     * <p>This method returns the information needed to create a STOMP subscription client-side.</p>
     * @return JSON representation of STOMP subscription information.
     */
    public JSONObject getSTOMPConfig() {
        JSONObject sc = new JSONObject();
        sc.put("endpointURL", this.endpointURL);
        sc.put("topicURL", this.topicURL);
        sc.put("port", this.port);
        return sc;
    }

    /**
     * <p>This method runs an asynchronous task that publishes session updates in intervals of milliseconds.</p>
     * @param sessionId The session ID to routinely check for updates.
     * @throws InterruptedException If the <code>Thread.sleep()</code> line is interrupted mid-execution.
     */
    @Async("archiveServerAsyncExecutor")
    public void runLiveSessionFeed(String sessionId) throws InterruptedException {
        String sessionTopic =  this.topicURL + "/" + sessionId;
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
            this.websocketTemplate.convertAndSend(sessionTopic, responseData);

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
