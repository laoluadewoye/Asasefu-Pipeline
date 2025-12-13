package com.laoluade.ingestor.ao3.services;

import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

@Service
public class ArchiveMessageService {
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
    public String getTestDataValue() { return "Hello Archive Ingestor Version 1 API!"; }
    public String getInfoGenericFailValue() { return "[Failed Unknown]"; }
    public String getDefaultRecordedMessage() { return "Starting new session..."; }
    public ZonedDateTime getNowTimestamp() { return ZonedDateTime.now(ZoneId.of("UTC")); }
    public String getNowTimestampString() { return this.getNowTimestamp().toString(); }
    public String getEmptyValue() { return ""; }

    // RESPONSE OBJECT MESSAGES
    // RESPONSE OBJECT MESSAGES
    public String getResponseBadURLFormat() { return "Sent link was not a proper URL format."; }
    public String getResponseNewChapterSession() { return "New chapter parsing session created."; }
    public String getResponseNewStorySession() { return "New story parsing session created."; }
    public String getResponseGetSessionFailed() { return "Session doesn't exist in database."; }
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
    public String createSpecDataMessage(String aiVersion, String otwVersion) {
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

    // MESSAGE CREATION - SESSION MANAGER
    // MESSAGE CREATION - SESSION MANAGER
    public String createASMAddedSessionMessage(String newSessionId) {
        return "Session manager added session " + newSessionId + " to session repository.";
    }
    public String createASMAddedSessionMapMessage(String newSessionId) {
        return "Session manager added session " + newSessionId + " to session map.";
    }
    public String createASMGetSessionMessage(String sessionId) {
        return "Session manager retrieved session " + sessionId + ".";
    }
    public String createASMGetSessionFailedMessage(String sessionId, String requestType) {
        return "Session manager could not retrieve session " + sessionId + " for " + requestType + ".";
    }
    public String createASMGetSessionCancelMessage(String sessionId) {
        return "Session manager retrieved canceled status for session " + sessionId + ".";
    }
    public String createASMGetLastResponseMessage(String sessionId) {
        return "Session manager retrieved last response for session " + sessionId + ".";
    }
    public String createASMGetLastResponseFailedMessage(String sessionId) {
        return "Session manager could not retrieve last response for session " + sessionId + ".";
    }
    public String createASMCancelSessionMessage(String sessionId) {
        return "Session manager canceled session " + sessionId + ".";
    }
    public String createASMCancelSessionFailedMessage(String sessionId) {
        return "Session manager could not cancel session " + sessionId + ".";
    }
    public String createASMUpdatedSessionFullMessage(String curSessionId) {
        return "Session manager updated all session information for " + curSessionId + ".";
    }
    public String createASMUpdateLastRecordedMessage(String curSessionId, String newLastRecordedMessage) {
        return "Session manager updated last recorded message for session " + curSessionId + " to: " + newLastRecordedMessage;
    }
    public String createASMUpdateChaptersTotal(String curSessionId, Integer chapterCount) {
        return "Session manager updated total chapter count for session " + curSessionId + " to: " + chapterCount;
    }
    public String createASMUpdateChaptersCompleted(String curSessionId, Integer chapterCount) {
        return "Session manager updated completed chapter count for session " + curSessionId + " to: " + chapterCount;
    }
    public String createASMPurgeCheckMessage(String curSessionId) {
        return "Session manager is checking entry " + curSessionId + " for purge.";
    }
    public String createASMPurgeSessionMessage(ArrayList<String> sessionsToDelete) {
        String ids = String.join(", ", sessionsToDelete);
        return "Session manager deleted the following stale sessions: " + ids + ".";
    }
    public String createASMPurgeAssertFailed(String curSessionId) {
        return "Session manager found session " + curSessionId + " in future map and was unable to verify in database for purging.";
    }
}
