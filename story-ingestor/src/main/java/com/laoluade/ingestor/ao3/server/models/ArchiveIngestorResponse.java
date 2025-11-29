package com.laoluade.ingestor.ao3.server.models;

public class ArchiveIngestorResponse {
    private String responseJSONString;

    public ArchiveIngestorResponse(String responseJSONString) { this.responseJSONString = responseJSONString;}

    public void setResponseJSONString(String responseJSONString) { this.responseJSONString = responseJSONString; }

    public String getResponseJSONString() { return this.responseJSONString; }
}
