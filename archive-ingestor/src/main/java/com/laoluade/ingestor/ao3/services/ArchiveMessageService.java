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
    public String getLoggingInfoActiveSessionFound() {
        return "Active Selenium session found. Closing it to start new session...";
    }
    public String getLoggingInfoNoActiveSessionFound() {
        return "No active Selenium session found. Starting new session...";
    }
    public String getLoggingInfoCreatedDriver() { return "Successfully created driver for parsing."; }
    public String getLoggingInfoQuitDriver() { return "Successfully quit driver after parsing."; }

    // LOGGING INFO MESSAGES - CHAPTER
    // LOGGING INFO MESSAGES - CHAPTER
    public String getLoggingInfoChapterObtainRequest() {
        return "Successfully obtained chapter parsing request contents.";
    }
    public String getLoggingInfoChapterParseSucceeded() { return "Successfully parsed link and extracted chapter."; }
    public String getLoggingInfoChapterRetrievedJSON() {
        return "Successfully retrieved JSON representation of new chapter.";
    }

    // LOGGING INFO MESSAGES - STORY
    // LOGGING INFO MESSAGES - STORY
    public String getLoggingInfoStoryObtainRequest() { return "Successfully obtained story parsing request contents."; }
    public String getLoggingInfoStoryParseSucceeded() { return "Successfully parsed link and extracted story."; }
    public String getLoggingInfoStoryRetrievedJSON() {
        return "Successfully retrieved JSON representation of new story.";
    }

    // LOGGING ERROR MESSAGES - GENERAL
    // LOGGING ERROR MESSAGES - GENERAL
    public String getLoggingErrorCreatedDriverFailed() {
        return "The driver service could not create a driver for parsing.";
    }
    public String getLoggingErrorParseFailedIO() { return "The archive ingestor could not read in json settings."; }
    public String getLoggingErrorParseFailedElement() {
        return "The archive ingestor could not find a required element during parsing.";
    }
    public String getLoggingErrorParseFailedCanceled() {
        return "The archive ingestor's task was canceled from parent service.";
    }
    public String getLoggingErrorParseFailedNotFound() {
        return "The archive ingestor came across the archive's 404 page and stopped parsing.";
    }
    public String getLoggingErrorBadParseType() {
        return "A bad parse type was fed into the archive ingestion service.";
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
    public String getTestDataValue() { return "Hello Archive Ingestor Version 1 API! API is Working!"; }
    public String getInfoGenericFailValue() { return "[Failed Unknown]"; }
    public String getDefaultRecordedMessage() { return "Starting new session..."; }
    public String getArchiveNotFoundPageTitle() { return "404 Error | Archive of Our Own"; }
    public ZonedDateTime getNowTimestamp() { return ZonedDateTime.now(ZoneId.of("UTC")); }
    public String getNowTimestampString() { return this.getNowTimestamp().toString(); }

    // RESPONSE OBJECT MESSAGES
    // RESPONSE OBJECT MESSAGES
    public String getResponseBadURLFormat() {
        return "Sent link was not a proper URL format. Ensure you are using a work link " +
                "(i.e. https://archiveofourown.org/works/XXXXXXX/chapters/XXXXXXXXX or one without the chapters bit)";
    }
    public String getResponseBadStoryLink() {
        return "Sent link was a chapter-specific link. Only send a work-wide link like " +
                "https://archiveofourown.org/works/XXXXXXX";
    }
    public String getResponseBadNicknameFormat() {
        return "Sent nickname was not a proper URL format. " +
                "Ensure your nickname has only alphanumeric characters, underscore, or hyphen";
    }
    public String getResponseBadSessionId() { return "Sent session ID was not in expected form."; }
    public String getResponseNewChapterSession() { return "New chapter parsing session created."; }
    public String getResponseNewStorySession() { return "New story parsing session created."; }
    public String getResponseGetSessionFailed() { return "Session doesn't exist in database."; }
    public String getResponseNewSessionFeed() { return "New live session websocket feed started."; }
    public String getResponseCancelSucceeded() { return "Sent cancel signal to the task."; }
    public String getResponseCancelFailed() { return "Session does not exist in session service."; }

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
    public String createURLExceptionMessage(String driverSocket) {
        return "Failed to create URL with driver address " + driverSocket + " for parsing.";
    }

    // MESSAGE CREATION - WEBSOCKET
    // MESSAGE CREATION - WEBSOCKET
    public String createWSSentMessage(String sessionId, String responseMessage) {
        return "Published session information for session " + sessionId + " to websocket feed." +
                "Latest message was: " + responseMessage;
    }

    // MESSAGE CREATION - SESSION SERVICE
    // MESSAGE CREATION - SESSION SERVICE
    public String createASSAddedSessionMessage(String newSessionId) {
        return "Session service added session " + newSessionId + " to session repository.";
    }
    public String createASSAddedSessionMapMessage(String newSessionId) {
        return "Session service added session " + newSessionId + " to session map.";
    }
    public String createASSGetSessionMessage(String sessionId) {
        return "Session service retrieved session " + sessionId + ".";
    }
    public String createASSGetSessionFailedMessage(String sessionId, String requestType) {
        return "Session service could not retrieve session " + sessionId + " for " + requestType + ".";
    }
    public String createASSGetSessionCancelMessage(String sessionId) {
        return "Session service retrieved canceled status for session " + sessionId + ".";
    }
    public String createASSCancelSessionMessage(String sessionId) {
        return "Session service canceled session " + sessionId + ".";
    }
    public String createASSCancelSessionFailedMessage(String sessionId) {
        return "Session service could not cancel session " + sessionId + ".";
    }
    public String createASSUpdatedSessionFullMessage(String curSessionId) {
        return "Session service updated all session information for " + curSessionId + ".";
    }
    public String createASSUpdateLastRecordedMessage(String curSessionId, String newLastRecordedMessage) {
        return "Session service updated last recorded message for session " +
                curSessionId + " to: " + newLastRecordedMessage;
    }
    public String createASSUpdateChaptersTotal(String curSessionId, Integer chapterCount) {
        return "Session service updated total chapter count for session " + curSessionId + " to: " + chapterCount;
    }
    public String createASSUpdateChaptersCompleted(String curSessionId, Integer chapterCount) {
        return "Session service updated completed chapter count for session " + curSessionId + " to: " + chapterCount;
    }
    public String createASSPurgeCheckMessage(String curSessionId) {
        return "Session service is checking entry " + curSessionId + " for purge.";
    }
    public String createASSPurgeSessionMessage(ArrayList<String> sessionsToDelete) {
        String ids = String.join(", ", sessionsToDelete);
        return "Session service deleted the following stale sessions: " + ids + ".";
    }
    public String createASSPurgeAssertFailed(String curSessionId) {
        return "Session service found session " + curSessionId +
                " in future map and was unable to verify in database for purging.";
    }
}
