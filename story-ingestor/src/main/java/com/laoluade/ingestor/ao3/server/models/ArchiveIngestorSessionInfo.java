package com.laoluade.ingestor.ao3.server.models;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ArchiveIngestorSessionInfo {
    private String creationTimestamp;
    private String sessionID;
    private String sessionNickname;
    private Integer chaptersCompleted;
    private Integer chaptersTotal;
    private String lastMessage;
    private boolean isFinished;
    private boolean isCanceled;

    // Empty constructor
    public ArchiveIngestorSessionInfo() {
        this.creationTimestamp = ZonedDateTime.now(ZoneId.of("UTC")).toString();
        this.sessionID = "";
        this.sessionNickname = "";
        this.chaptersCompleted = 0;
        this.chaptersTotal = 0;
        this.lastMessage = "Started using empty constructor.";
        this.isFinished = false;
        this.isCanceled = false;
    }

    // Default constructor
    public ArchiveIngestorSessionInfo(String sessionID, String sessionNickname) {
        this.creationTimestamp = ZonedDateTime.now(ZoneId.of("UTC")).toString();
        this.sessionID = sessionID;
        this.sessionNickname = sessionNickname;
        this.chaptersCompleted = 0;
        this.chaptersTotal = 0;
        this.lastMessage = "Started using default constructor.";
        this.isFinished = false;
        this.isCanceled = false;
    }

    public void refreshCreationTimestamp() {
        this.creationTimestamp = ZonedDateTime.now(ZoneId.of("UTC")).toString();
    }

    public void setCreationTimestamp(ZonedDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp.toString();
    }

    public void setCreationTimestamp(String creationTimestamp) { this.creationTimestamp = creationTimestamp;}

    public String getCreationTimestamp() { return this.creationTimestamp; }

    public void setSessionID(String sessionID) { this.sessionID = sessionID; }

    public String getSessionID() { return this.sessionID; }

    public void setSessionNickname(String sessionNickname) { this.sessionNickname = sessionNickname; }

    public String getSessionNickname() { return this.sessionNickname; }

    public void setChaptersCompleted(Integer chaptersCompleted) { this.chaptersCompleted = chaptersCompleted; }

    public Integer getChaptersCompleted() { return this.chaptersCompleted; }

    public void setChaptersTotal(Integer chaptersTotal) { this.chaptersTotal = chaptersTotal; }

    public Integer getChaptersTotal() { return this.chaptersTotal; }

    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getLastMessage() { return this.lastMessage; }

    public void setIsFinished(boolean isFinished) { this.isFinished = isFinished; }

    public boolean getIsFinished() { return this.isFinished; }

    public void setIsCanceled(boolean isCanceled) { this.isCanceled = isCanceled; }

    public boolean getIsCanceled() { return this.isCanceled; }
}
