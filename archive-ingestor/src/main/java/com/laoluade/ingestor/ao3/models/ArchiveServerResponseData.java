package com.laoluade.ingestor.ao3.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveServerResponseData {
    private String sessionId = "";
    private String sessionNickname = "";
    private boolean sessionFinished = false;
    private boolean sessionCanceled = false;
    private boolean sessionException = false;
    private int parseChaptersCompleted = 0;
    private int parseChaptersTotal = 0;
    private String parseResult = "";
    private String responseMessage;

    public ArchiveServerResponseData(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public ArchiveServerResponseData(String sessionId, String responseMessage) {
        this.sessionId = sessionId;
        this.responseMessage = responseMessage;
    }

    public ArchiveServerResponseData(String sessionId, String sessionNickname, String responseMessage) {
        this.sessionId = sessionId;
        this.sessionNickname = sessionNickname;
        this.responseMessage = responseMessage;
    }
}
