package com.laoluade.ingestor.ao3.services;

import com.laoluade.ingestor.ao3.models.ArchiveServerResponseData;
import com.laoluade.ingestor.ao3.repositories.ArchiveParse;
import com.laoluade.ingestor.ao3.repositories.ArchiveSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ArchiveWebsocketService {
    @Autowired
    private final ArchiveLogService logService;

    @Autowired
    private final ArchiveMessageService messageService;

    @Autowired
    private final ArchiveSessionService sessionService;

    @Autowired
    private final SimpMessagingTemplate websocketTemplate;

    private final Integer sendIntervalMilli;

    public ArchiveWebsocketService(ArchiveLogService logService, ArchiveMessageService messageService,
                                   ArchiveSessionService sessionService, SimpMessagingTemplate websocketTemplate,
                                   @Value("${archiveServer.websocket.sendIntervalMilli:1000}") Integer sendIntervalMilli) {
        this.logService = logService;
        this.messageService = messageService;
        this.sessionService = sessionService;
        this.websocketTemplate = websocketTemplate;
        this.sendIntervalMilli = sendIntervalMilli;
    }

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
