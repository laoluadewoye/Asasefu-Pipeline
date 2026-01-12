package com.laoluade.ingestor.ao3.services;

import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * <p>This class is the Spring Boot service responsible for storing standard messages for the server to use.</p>
 */
@Service
public class ArchiveMessageService {
    // LOGGING INFO MESSAGES - GENERAL
    // LOGGING INFO MESSAGES - GENERAL
    /**
     * <p>This method returns a string for logging that the test API string is sent.</p>
     * @return The logging string.
     */
    public String getLoggingInfoTestAPISend() { return "Sending test API string."; }

    /**
     * <p>This method returns a string for logging that an active Selenium session was found.</p>
     * @return The logging string.
     */
    public String getLoggingInfoActiveSessionFound() {
        return "Active Selenium session found. Closing it to start new session...";
    }

    /**
     * <p>This method returns a string for logging that an active Selenium session was not found.</p>
     * @return The logging string.
     */
    public String getLoggingInfoNoActiveSessionFound() {
        return "No active Selenium session found. Starting new session...";
    }

    /**
     * <p>This method returns a string for logging that a Selenium driver was successfully created.</p>
     * @return The logging string.
     */
    public String getLoggingInfoCreatedDriver() { return "Successfully created driver for parsing."; }

    /**
     * <p>This method returns a string for logging that a Selenium driver was quit.</p>
     * @return The logging string.
     */
    public String getLoggingInfoQuitDriver() { return "Successfully quit driver after parsing."; }

    // LOGGING INFO MESSAGES - CHAPTER
    // LOGGING INFO MESSAGES - CHAPTER
    /**
     * <p>This method returns a string for logging that a chapter parsing request was successfully read.</p>
     * @return The logging string.
     */
    public String getLoggingInfoChapterObtainRequest() {
        return "Successfully obtained chapter parsing request contents.";
    }

    /**
     * <p>This method returns a string for logging that a chapter parsing request was successfully completed.</p>
     * @return The logging string.
     */
    public String getLoggingInfoChapterParseSucceeded() { return "Successfully parsed link and extracted chapter."; }

    /**
     * <p>This method returns a string for logging that a chapter's JSON was successfully obtained.</p>
     * @return The logging string.
     */
    public String getLoggingInfoChapterRetrievedJSON() {
        return "Successfully retrieved JSON representation of new chapter.";
    }

    // LOGGING INFO MESSAGES - STORY
    // LOGGING INFO MESSAGES - STORY
    /**
     * <p>This method returns a string for logging that a story parsing request was successfully read.</p>
     * @return The logging string.
     */
    public String getLoggingInfoStoryObtainRequest() { return "Successfully obtained story parsing request contents."; }

    /**
     * <p>This method returns a string for logging that a story parsing request was successfully completed.</p>
     * @return The logging string.
     */
    public String getLoggingInfoStoryParseSucceeded() { return "Successfully parsed link and extracted story."; }

    /**
     * <p>This method returns a string for logging that a story's JSON was successfully obtained.</p>
     * @return The logging string.
     */
    public String getLoggingInfoStoryRetrievedJSON() {
        return "Successfully retrieved JSON representation of new story.";
    }

    // LOGGING ERROR MESSAGES - GENERAL
    // LOGGING ERROR MESSAGES - GENERAL
    /**
     * <p>This method returns a string for logging that a Selenium driver could not be created.</p>
     * @return The logging string.
     */
    public String getLoggingErrorCreatedDriverFailed() {
        return "The driver service could not create a driver for parsing.";
    }

    /**
     * <p>This method returns a string for logging that the archive ingestor could not read in JSON settings.</p>
     * @return The logging string.
     */
    public String getLoggingErrorParseFailedIO() { return "The archive ingestor could not read in json settings."; }

    /**
     * <p>This method returns a string for logging that the archive ingestor could not find an HTML element.</p>
     * @return The logging string.
     */
    public String getLoggingErrorParseFailedElement() {
        return "The archive ingestor could not find a required element during parsing.";
    }

    /**
     * <p>This method returns a string for logging that the archive ingestor's task was canceled.</p>
     * @return The logging string.
     */
    public String getLoggingErrorParseFailedCanceled() {
        return "The archive ingestor's task was canceled from parent service.";
    }

    /**
     * <p>This method returns a string for logging that the archive ingestor's parse failed because it hit a 404 page.</p>
     * @return The logging string.
     */
    public String getLoggingErrorParseFailedNotFound() {
        return "The archive ingestor came across the archive's 404 page and stopped parsing.";
    }

    /**
     * <p>This method returns a string for logging that the archive ingestor's parse failed due to an unknown reason.</p>
     * @return The logging string.
     */
    public String getLoggingErrorParseFailedUnknown() {
        return "The archive ingestor failed due to an unknown reason. Please let the creator know and " +
                "add the settings you used for the parse.";
    }

    /**
     * <p>This method returns a string for logging that the archive ingestor's parse failed due to a bad parse type.</p>
     * @return The logging string.
     */
    public String getLoggingErrorBadParseType() {
        return "A bad parse type was fed into the archive ingestion service.";
    }

    // LOGGING ERROR MESSAGES - CHAPTER
    // LOGGING ERROR MESSAGES - CHAPTER
    /**
     * <p>
     *     This method returns a string for logging that the archive ingestor's chapter parse failed due to a sleep
     *     cycle being interrupted.
     * </p>
     * @return The logging string.
     */
    public String getLoggingErrorChapterFailedInterrupt() {
        return "The execution was unexpectedly interrupted during Thread.sleep() during chapter parsing.";
    }

    /**
     * <p>
     *     This method returns a string for logging that the archive ingestor's parse failed due to a chapter
     *     missing paragraphs.
     * </p>
     * @return The logging string.
     */
    public String getLoggingErrorChapterFailedContent() { return "The chapter's paragraphs could not be found."; }

    // LOGGING ERROR MESSAGES - STORY
    // LOGGING ERROR MESSAGES - STORY
    /**
     * <p>
     *     This method returns a string for logging that the archive ingestor's story parse failed due to a sleep
     *     cycle being interrupted.
     * </p>
     * @return The logging string.
     */
    public String getLoggingErrorStoryFailedInterrupt() {
        return "The execution was unexpectedly interrupted during Thread.sleep() during story parsing.";
    }
    /**
     * <p>
     *     This method returns a string for logging that the archive ingestor's parse failed due to the story missing
     *     chapter paragraphs.
     * </p>
     * @return The logging string.
     */
    public String getLoggingErrorStoryFailedContent() { return "One of the chapter's paragraphs could not be found."; }

    // NON-RESPONSE OBJECT MESSAGES
    // NON-RESPONSE OBJECT MESSAGES
    /**
     * <p>This method returns a test string for testing the archive server's API.</p>
     * @return The test string.
     */
    public String getTestDataValue() { return "Hello Archive Ingestor Version 1 API! API is Working!"; }

    /**
     * <p>This method returns a generic fail value.</p>
     * @return The generic fail string.
     */
    public String getInfoGenericFailValue() { return "[Failed Unknown]"; }

    /**
     * <p>
     *     This method returns a default message for new
     *     {@link com.laoluade.ingestor.ao3.repositories.ArchiveSession} entities.
     * </p>
     * @return The default string.
     */
    public String getDefaultRecordedMessage() { return "Starting new session..."; }

    /**
     * <p>This method returns the expected AO3 404 Error page title.</p>
     * @return The AO3 404 page title string.
     */
    public String getArchiveNotFoundPageTitle() { return "404 Error | Archive of Our Own"; }

    /**
     * <p>This method returns a current timestamp as a {@link ZonedDateTime} object.</p>
     * @return The current timestamp as a {@link ZonedDateTime} object.
     */
    public ZonedDateTime getNowTimestamp() { return ZonedDateTime.now(ZoneId.of("UTC")); }

    /**
     * <p>This method returns a current timestamp as a string.</p>
     * @return The current timestamp string.
     */
    public String getNowTimestampString() { return this.getNowTimestamp().toString(); }

    // RESPONSE OBJECT MESSAGES
    // RESPONSE OBJECT MESSAGES
    /**
     * <p>This method returns a message response letting the client know the page link was badly formatted.</p>
     * @return The response message string.
     */
    public String getResponseBadURLFormat() {
        return "Sent link was not a proper URL format. Ensure you are using a work link " +
                "(i.e. https://archiveofourown.org/works/XXXXXXX/chapters/XXXXXXXXX or one without the chapters bit)";
    }

    /**
     * <p>This method returns a message response letting the client know a chapter link was sent for story parsing.</p>
     * @return The response message string.
     */
    public String getResponseBadStoryLink() {
        return "Sent link was a chapter-specific link. Only send a work-wide link like " +
                "https://archiveofourown.org/works/XXXXXXX";
    }

    /**
     * <p>This method returns a message response letting the client know the nickname was badly formatted.</p>
     * @return The response message string.
     */
    public String getResponseBadNicknameFormat() {
        return "Sent nickname was not a proper URL format. " +
                "Ensure your nickname has only alphanumeric characters, underscore, or hyphen";
    }

    /**
     * <p>This method returns a message response letting the client know the session ID was badly formatted.</p>
     * @return The response message string.
     */
    public String getResponseBadSessionId() { return "Sent session ID was not in expected form."; }

    /**
     * <p>This method returns a message response letting the client know a new chapter parsing session was created.</p>
     * @return The response message string.
     */
    public String getResponseNewChapterSession() { return "New chapter parsing session created."; }

    /**
     * <p>This method returns a message response letting the client know a new story parsing session was created.</p>
     * @return The response message string.
     */
    public String getResponseNewStorySession() { return "New story parsing session created."; }

    /**
     * <p>This method returns a message response letting the client know a session couldn't be found in database.</p>
     * @return The response message string.
     */
    public String getResponseGetSessionFailed() { return "Session doesn't exist in database."; }

    /**
     * <p>This method returns a message response letting the client know a new websocket feed was started.</p>
     * @return The response message string.
     */
    public String getResponseNewSessionFeed() { return "New live session websocket feed started."; }

    /**
     * <p>This method returns a message response letting the client know a cancel signal was sent to a session.</p>
     * @return The response message string.
     */
    public String getResponseCancelSucceeded() { return "Sent cancel signal to the task."; }

    /**
     * <p>This method returns a message response letting the client know the session could not be canceled.</p>
     * @return The response message string.
     */
    public String getResponseCancelFailed() { return "Session does not exist in session service."; }

    // MESSAGE CREATION - GENERAL
    // MESSAGE CREATION - GENERAL
    /**
     * <p>This method creates a message saying the driver socket is set to a specific URL.</p>
     * @param driverSocket The URL the driver socket is set to.
     * @return The created message string.
     */
    public String createDriverSocketMessage(String driverSocket) {
        if (driverSocket != null) {
            return "Driver socket is set to " + driverSocket + ".";
        }
        else {
            return "Driver socket is set to nothing.";
        }
    }

    /**
     * <p>This method creates a message saying the session persistence setting is set to a specific amount of seconds.</p>
     * @param sessionPersistSecs The amount of seconds a session should persist in the session service for.
     * @return The created message string.
     */
    public String createSessionPersistSecsMessage(Integer sessionPersistSecs) {
        return "Session persistence is set to " + sessionPersistSecs + " seconds.";
    }

    /**
     * <p>
     *     This method creates a message saying that the session validity check interval is set to a specific
     *     amount of milliseconds.
     * </p>
     * @param checkIntervalMilli The size of the check interval in milliseconds.
     * @return The created message string.
     */
    public String createSessionCheckIntervalMessage(Integer checkIntervalMilli) {
        return "Session validity check interval is set to " + checkIntervalMilli + " milliseconds.";
    }

    /**
     * <p>
     *     This method creates a message saying that the websocket publishing interval is set to a specific amount
     *     of milliseconds.
     * </p>
     * @param sendIntervalMilli The size of the publishing interval in milliseconds.
     * @return The created message string.
     */
    public String createWebsocketSendIntervalMessage(Integer sendIntervalMilli) {
        return "Websocket publishing interval is set to " + sendIntervalMilli + " milliseconds.";
    }

    /**
     * <p>This method creates a message saying that the websocket endpoint URL is set to a specific URL.</p>
     * @param endpointURL The chosen URL.
     * @return The created message string.
     */
    public String createWebsocketEndpointMessage(String endpointURL) {
        return "Websocket endpoint URL is set to " + endpointURL + ".";
    }

    /**
     * <p>This method creates a message saying that the websocket topic URL is set to a specific URL.</p>
     * @param topicURL The chosen URL.
     * @return The created message string.
     */
    public String createWebsocketTopicMessage(String topicURL) {
        return "Websocket topic URL is set to " + topicURL + ".";
    }

    /**
     * <p>This method creates a message saying that the websocket app API is set to a specific URL.</p>
     * @param appURL The chosen URL.
     * @return The created message string.
     */
    public String createWebsocketAppMessage(String appURL) {
        return "Websocket app URL is set to " + appURL + ".";
    }

    /**
     * <p>This method creates a message saying that the websocket port is set to a specific port number.</p>
     * @param port The port number.
     * @return The created message string.
     */
    public String createWebsocketPortMessage(Integer port) {
        return "Websocket port is set to " + port + ".";
    }

    /**
     * <p>This method creates a message saying that the application specification data is being sent.</p>
     * @param aiVersion The archive ingestor version number.
     * @param otwVersion The latest supported OTW Archive version number.
     * @return The created message string.
     */
    public String createSpecDataMessage(String aiVersion, String otwVersion) {
        return "Sending version number " + aiVersion + " and OTW Archive version " + otwVersion + ".";
    }

    /**
     * <p>This method creates a message saying that the driver service failed to create a driver.</p>
     * @param driverSocket The socket address to connect the driver to.
     * @return The created message string.
     */
    public String createURLExceptionMessage(String driverSocket) {
        return "Failed to create URL with driver address " + driverSocket + " for parsing.";
    }

    // MESSAGE CREATION - WEBSOCKET
    // MESSAGE CREATION - WEBSOCKET
    /**
     * <p>This method creates a message saying that the websocket service published a new message.</p>
     * @param sessionId The session ID being live fed.
     * @param responseMessage The response message to publish.
     * @return The created message string.
     */
    public String createWSSentMessage(String sessionId, String responseMessage) {
        return "Published session information for session " + sessionId + " to websocket feed. " +
                "Latest message was: " + responseMessage;
    }

    // MESSAGE CREATION - SESSION SERVICE
    // MESSAGE CREATION - SESSION SERVICE
    /**
     * <p>This method creates a message saying that the session service added a session to persistent storage.</p>
     * @param newSessionId The session ID.
     * @return The created message string.
     */
    public String createASSAddedSessionMessage(String newSessionId) {
        return "Session service added session " + newSessionId + " to session repository.";
    }

    /**
     * <p>This method creates a message saying that the session service added a session to an in-memory map.</p>
     * @param newSessionId The session ID.
     * @return The created message string.
     */
    public String createASSAddedSessionMapMessage(String newSessionId) {
        return "Session service added session " + newSessionId + " to session map.";
    }

    /**
     * <p>This method creates a message saying that the session service retrieved information for a session.</p>
     * @param sessionId The session ID.
     * @return The created message string.
     */
    public String createASSGetSessionMessage(String sessionId) {
        return "Session service retrieved session " + sessionId + ".";
    }

    /**
     * <p>This method creates a message saying that the session service could not retrieve information for a session.</p>
     * @param sessionId The session ID.
     * @param requestType The type of request being made to the session service.
     * @return The created message string.
     */
    public String createASSGetSessionFailedMessage(String sessionId, String requestType) {
        return "Session service could not retrieve session " + sessionId + " for " + requestType + ".";
    }

    /**
     * <p>This method creates a message saying that the session service retrieved the canceled status for a session.</p>
     * @param sessionId The session ID.
     * @return The created message string.
     */
    public String createASSGetSessionCancelMessage(String sessionId) {
        return "Session service retrieved canceled status for session " + sessionId + ".";
    }

    /**
     * <p>This method creates a message saying that the session service canceled an active session.</p>
     * @param sessionId The session ID.
     * @return The created message string.
     */
    public String createASSCancelSessionMessage(String sessionId) {
        return "Session service canceled session " + sessionId + ".";
    }

    /**
     * <p>This method creates a message saying that the session service could not cancel an active session.</p>
     * @param sessionId The session ID.
     * @return The created message string.
     */
    public String createASSCancelSessionFailedMessage(String sessionId) {
        return "Session service could not cancel session " + sessionId + ".";
    }

    /**
     * <p>This method creates a message saying that the session service updated a session's information.</p>
     * @param curSessionId The session ID.
     * @return The created message string.
     */
    public String createASSUpdatedSessionFullMessage(String curSessionId) {
        return "Session service updated all session information for " + curSessionId + ".";
    }

    /**
     * <p>This method creates a message saying that the session service updated a session's last recorded message.</p>
     * @param curSessionId The session ID.
     * @param newLastRecordedMessage The new last recorded message.
     * @return The created message string.
     */
    public String createASSUpdateLastRecordedMessage(String curSessionId, String newLastRecordedMessage) {
        return "Session service updated last recorded message for session " +
                curSessionId + " to: " + newLastRecordedMessage;
    }

    /**
     * <p>This method creates a message saying that the session service updated a session's total chapter count.</p>
     * @param curSessionId The session ID.
     * @param chapterCount The new chapter count.
     * @return The created message string.
     */
    public String createASSUpdateChaptersTotal(String curSessionId, Integer chapterCount) {
        return "Session service updated total chapter count for session " + curSessionId + " to: " + chapterCount;
    }

    /**
     * <p>This method creates a message saying that the session service updated a session's completed chapter count.</p>
     * @param curSessionId The session ID.
     * @param chapterCount The new chapter count.
     * @return The created message string.
     */
    public String createASSUpdateChaptersCompleted(String curSessionId, Integer chapterCount) {
        return "Session service updated completed chapter count for session " + curSessionId + " to: " + chapterCount;
    }

    /**
     * <p>This method creates a message saying that the session service checked if a session should be purged.</p>
     * @param curSessionId The session ID.
     * @return The created message string.
     */
    public String createASSPurgeCheckMessage(String curSessionId) {
        return "Session service is checking entry " + curSessionId + " for purge.";
    }

    /**
     * <p>
     *     This method creates a message saying that the session service deleted a list of sessions from
     *     in-memory map tracking.
     * </p>
     * @param sessionsToDelete The session IDs that were deleted.
     * @return The created message string.
     */
    public String createASSPurgeSessionMessage(ArrayList<String> sessionsToDelete) {
        String ids = String.join(", ", sessionsToDelete);
        return "Session service deleted the following stale sessions: " + ids + ".";
    }

    /**
     * <p>This method creates a message saying that the session service found a session in tracking but not in database.</p>
     * @param curSessionId The session ID.
     * @return The created message string.
     */
    public String createASSPurgeAssertFailed(String curSessionId) {
        return "Session service found session " + curSessionId +
                " in future map and was unable to verify in database for purging.";
    }
}
