package com.laoluade.ingestor.ao3.models;

public class ArchiveIngestorTaskFuture {
    private String resultMessage;
    private boolean isSuccess;

    public ArchiveIngestorTaskFuture(String resultMessage, boolean isSuccess) {
        this.resultMessage = resultMessage;
        this.isSuccess = isSuccess;
    }

    public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }

    public String getResultMessage() { return this.resultMessage; }

    public void setIsSuccess(boolean isSuccess) { this.isSuccess = isSuccess; }

    public boolean getIsSuccess() { return this.isSuccess; }
}
