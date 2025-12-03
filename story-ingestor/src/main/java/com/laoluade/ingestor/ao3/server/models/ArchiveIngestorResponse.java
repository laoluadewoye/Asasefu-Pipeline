package com.laoluade.ingestor.ao3.server.models;

public class ArchiveIngestorResponse {
    private String responseJSONString;
    private String resultMessage;
    private ArchiveIngestorSessionInfo sessionInfo;

    public ArchiveIngestorResponse(String responseJSONString, String resultMessage) {
        this.responseJSONString = responseJSONString;
        this.resultMessage = resultMessage;
        this.sessionInfo = new ArchiveIngestorSessionInfo();
    }

    public ArchiveIngestorResponse(String responseJSONString, String resultMessage,
                                   ArchiveIngestorSessionInfo sessionInfo) {
        this.responseJSONString = responseJSONString;
        this.resultMessage = resultMessage;
        this.sessionInfo = sessionInfo;
    }

    public void setResponseJSONString(String responseJSONString) { this.responseJSONString = responseJSONString; }

    public String getResponseJSONString() { return this.responseJSONString; }

    public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }

    public String getResultMessage() { return this.resultMessage; }

    public void setSessionInfo(ArchiveIngestorSessionInfo sessionInfo) { this.sessionInfo = sessionInfo; }

    public ArchiveIngestorSessionInfo getSessionInfo() { return this.sessionInfo; }
}
