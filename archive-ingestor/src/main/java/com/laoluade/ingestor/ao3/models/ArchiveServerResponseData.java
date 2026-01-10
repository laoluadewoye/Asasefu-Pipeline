package com.laoluade.ingestor.ao3.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>This class defines the POJO model for response data for the Archive Server.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveServerResponseData {
    /**
     * <p>This attribute holds the session's ID.</p>
     */
    private String sessionId = "";

    /**
     * <p>This attribute holds the client's nickname for the session.</p>
     */
    private String sessionNickname = "";

    /**
     * <p>This attribute holds the flag for if a session finished successfully.</p>
     */
    private boolean sessionFinished = false;

    /**
     * <p>This attribute holds the flag for if a session got canceled.</p>
     */
    private boolean sessionCanceled = false;

    /**
     * <p>This attribute holds the flag for if a session had an exception error.</p>
     */
    private boolean sessionException = false;

    /**
     * <p>This attribute holds the tally of completed chapters.</p>
     */
    private int parseChaptersCompleted = 0;

    /**
     * <p>This attribute holds the tally of total chapters.</p>
     */
    private int parseChaptersTotal = 0;

    /**
     * <p>This attribute holds the JSON string from a completed parse session, or an empty string.</p>
     */
    private String parseResult = "";

    /**
     * <p>This attribute holds the response message for the archive server.</p>
     */
    private String responseMessage;

    /**
     * <p>This constructor creates a new archive server response with just a response message.</p>
     * @param responseMessage The response message for the archive server.
     */
    public ArchiveServerResponseData(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    /**
     * <p>This constructor creates a new archive server response with a session ID and response message.</p>
     * @param sessionId The current parse session's ID.
     * @param responseMessage The response message for the archive server.
     */
    public ArchiveServerResponseData(String sessionId, String responseMessage) {
        this.sessionId = sessionId;
        this.responseMessage = responseMessage;
    }

    /**
     * <p>
     *     This constructor creates a new archive server response with a session ID, session nickname,
     *     and response message.
     * </p>
     * @param sessionId The current parse session's ID.
     * @param sessionNickname The client's nickname for the session.
     * @param responseMessage The response message for the archive server.
     */
    public ArchiveServerResponseData(String sessionId, String sessionNickname, String responseMessage) {
        this.sessionId = sessionId;
        this.sessionNickname = sessionNickname;
        this.responseMessage = responseMessage;
    }
}
