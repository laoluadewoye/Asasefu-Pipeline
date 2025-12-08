package com.laoluade.ingestor.ao3.services;

import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ArchiveIngestorMessageManager {
    // LOGGING INFO MESSAGES - GENERAL
    // LOGGING INFO MESSAGES - GENERAL
    public String getLoggingInfoTestAPISend() { return "Sending test API string."; }

    // LOGGING INFO MESSAGES - CHAPTER
    // LOGGING INFO MESSAGES - CHAPTER
    public String getLoggingInfoChapterObtainRequest() {
        return "Successfully obtained chapter parsing request contents.";
    }
    public String getLoggingInfoChapterCreatedDriver() { return "Successfully created driver for chapter parsing."; }
    public String getLoggingInfoChapterParseSucceeded() { return "Successfully parsed link and extracted chapter."; }
    public String getLoggingInfoChapterQuitDriver() { return "Successfully quit driver for chapter parsing."; }
    public String getLoggingInfoChapterRetrievedJSON() {
        return "Successfully retrieved JSON representation of new chapter.";
    }

    // LOGGING INFO MESSAGES - STORY
    // LOGGING INFO MESSAGES - STORY
    public String getLoggingInfoStoryObtainRequest() { return "Successfully obtained story parsing request contents."; }
    public String getLoggingInfoStoryCreatedDriver() { return "Successfully created driver for story parsing."; }
    public String getLoggingInfoStoryParseSucceeded() { return "Successfully parsed link and extracted story."; }
    public String getLoggingInfoStoryQuitDriver() { return "Successfully quit driver for story parsing."; }
    public String getLoggingInfoStoryRetrievedJSON() {
        return "Successfully retrieved JSON representation of new story.";
    }

    // LOGGING ERROR MESSAGES - GENERAL
    // LOGGING ERROR MESSAGES - GENERAL
    public String getLoggingErrorParseFailedIO() { return "The archive ingestor could not read in json settings."; }
    public String getLoggingErrorParseFailedElement() {
        return "The archive ingestor could not find a required element during parsing.";
    }
    public String getLoggingErrorParseFailedCanceled() {
        return "The archive ingestor's task was canceled from parent service.";
    }

    // LOGGING ERROR MESSAGES - CHAPTER
    // LOGGING ERROR MESSAGES - CHAPTER
    public String getLoggingErrorChapterFailedInterrupt() {
        return "The execution was unexpectedly interrupted during Thread.sleep() during chapter parsing.";
    }
    public String getLoggingErrorChapterFailedContent() { return "The chapter's paragraphs could not be found."; }

    // LOGGING ERROR MESSAGES - STORY
    // LOGGING ERROR MESSAGES - STORY
    public String getLoggingErrorStoryFailedInterrupt() {
        return "Execution was unexpectedly interrupted during Thread.sleep() during story parsing.";
    }
    public String getLoggingErrorStoryFailedContent() { return "One of the chapter's paragraphs could not be found."; }

    // NON-RESPONSE OBJECT MESSAGES
    // NON-RESPONSE OBJECT MESSAGES
    public String getTestAPIInfo() { return "Hello Archive Ingestor Version 1 API!"; }
    public String getInfoIOFailValue() { return "[Failed IO]"; }
    public String getInfoGenericFailValue() { return "[Failed Unknown]"; }
    public String getUTCValue() { return "UTC"; }
    public String getEmptyValue() { return ""; }

    // RESPONSE OBJECT MESSAGES
    // RESPONSE OBJECT MESSAGES
    public String getResponseBadURLFormat() { return "Sent link was not a proper URL format."; }
    public String getResponseNewChapterSession() { return "New chapter parsing session created."; }
    public String getResponseNewStorySession() { return "New story parsing session created."; }
    public String getResponseGetSessionFailed() { return "Session either doesn't exist or has been deleted."; }
    public String getResponseCancelSucceeded() { return "Sent cancel signal to the task."; }
    public String getResponseCancelFailed() { return "Session does not exist in session manager."; }

    // MESSAGE CREATION - GENERAL
    // MESSAGE CREATION - GENERAL
    public String createDriverSocketMessage(String driverSocket) {
        if (driverSocket != null) {
            return "Driver socket is set to " + driverSocket + ".";
        }
        else {
            return "Driver socket is set to nothing.";
        }
    }
    public String createSessionPersistSecsMessage(Integer sessionPersistSecs) {
        return "Session persistence is set to " + sessionPersistSecs + " seconds.";
    }
    public String createSessionCheckIntervalMessage(Integer checkIntervalMilli) {
        return "Session validity check interval is set to is set to " + checkIntervalMilli + " seconds.";
    }
    public String createInfoSendingMessage(String aiVersion, String otwVersion) {
        return "Sending version number " + aiVersion + " and OTW Archive version " + otwVersion + ".";
    }

    // MESSAGE CREATION - CHAPTER
    // MESSAGE CREATION - CHAPTER
    public String createChapterURLExceptionMessage(String driverSocket) {
        return "Failed to create URL with driver address " + driverSocket + " for chapter parsing.";
    }

    // MESSAGE CREATION - STORY
    // MESSAGE CREATION - STORY
    public String createStoryURLExceptionMessage(String driverSocket) {
        return "Failed to create URL with driver address " + driverSocket + " for story parsing.";
    }

    // MESSAGE CREATION - SESSION MANAGER createAISMUpdatedSessionMessage
    // MESSAGE CREATION - SESSION MANAGER
    public String createAISMAddedSessionMessage(String newSessionID) {
        return "Session manager added session " + newSessionID + ".";
    }
    public String createAISMGetSessionMessage(String sessionID) {
        return "Session manager retrieved session " + sessionID + ".";
    }
    public String createAISMGetSessionFailedMessage(String sessionID) {
        return "Session manager could not retrieve session " + sessionID + " and returned null.";
    }
    public String createAISMCancelSessionMessage(String sessionID) {
        return "Session manager canceled session " + sessionID + ".";
    }
    public String createAISMCancelSessionFailedMessage(String sessionID) {
        return "Session manager could not cancel session " + sessionID + ".";
    }
    public String createAISMDeleteSessionMessage(ArrayList<String> sessionsToDelete) {
        String ids = String.join(", ", sessionsToDelete);
        return "Session manager deleted the following stale sessions: " + ids + ".";
    }
    public String createAISMUpdatedSessionMessage(String curSessionID) {
        return "Session manager updated session " + curSessionID + ".";
    }
}
