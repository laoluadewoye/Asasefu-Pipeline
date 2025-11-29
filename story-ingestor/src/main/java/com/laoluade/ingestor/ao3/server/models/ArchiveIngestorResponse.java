package com.laoluade.ingestor.ao3.server.models;

public class ArchiveIngestorResponse {
    private String responseJSONString;
    private String resultMessage;

    public ArchiveIngestorResponse(String responseJSONString, String resultMessage) {
        this.responseJSONString = responseJSONString;
        this.resultMessage = resultMessage;
    }

    public void setResponseJSONString(String responseJSONString) { this.responseJSONString = responseJSONString; }

    public String getResponseJSONString() { return this.responseJSONString; }

    public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }

    public String getResultMessage() { return this.resultMessage; }
}
