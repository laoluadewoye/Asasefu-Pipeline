package com.laoluade.ingestor.ao3.controllers;

// Server classes
import com.laoluade.ingestor.ao3.models.ArchiveServerSpecData;
import com.laoluade.ingestor.ao3.models.ArchiveServerRequestData;
import com.laoluade.ingestor.ao3.models.ArchiveServerResponseData;
import com.laoluade.ingestor.ao3.models.ArchiveServerTestData;
import com.laoluade.ingestor.ao3.repositories.ArchiveParseType;
import com.laoluade.ingestor.ao3.services.ArchiveService;

// Spring Boot classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>This class configures where different services can be reached through the server REST API.</p>
 * <p>
 *     This class uses a mix of {@link GetMapping} and {@link PostMapping},
 *     including a GET request to start a live information session.
 *     Any external application using the live info service is expected to
 *     manually subscribe to the websocket the Archive Server exposes.
 * </p>
 */
@RestController
public class ArchiveController {
    /**
     * <p>This attribute corresponds to the ArchiveService service that is managed by Spring Boot.</p>
     */
    @Autowired
    ArchiveService archiveService;

    /**
     * <p>This method returns test data to ensure the API is reachable.</p>
     * <p>Spring Boot attaches this method to the <code>/api/v1</code> endpoint and formats the response as JSON.</p>
     * @return {@link ArchiveServerTestData} that is formatted into JSON by Spring Boot.
     */
    @GetMapping("/api/v1")
    public ArchiveServerTestData getArchiveIngestorTestData() {
        return archiveService.getArchiveIngestorTestData();
    }

    /**
     * <p>This method returns specification data about the Archive Server.</p>
     * <p>Spring Boot attaches this method to the <code>/api/v1/spec</code> endpoint and formats the response as JSON.</p>
     * @return {@link ArchiveServerSpecData} that is formatted into JSON by Spring Boot.
     */
    @GetMapping("/api/v1/spec")
    public ArchiveServerSpecData getArchiveServerSpecData() {
        return archiveService.getArchiveServerSpecData();
    }

    /**
     * <p>
     *     This method accepts a chapter parsing request formatted as a {@link ArchiveServerRequestData} object
     *     and returns a confirmation that the request was accepted or rejected.
     * </p>
     * <p>
     *     Spring Boot attaches this method to the <code>/api/v1/parse/chapter</code> endpoint,
     *     creates an {@link ArchiveServerRequestData} object from the POST request body,
     *     and formats the response as JSON.
     * </p>
     * @param request The POST request body, formatted into an {@link ArchiveServerRequestData} object.
     * @return {@link ArchiveServerResponseData} that is formatted into JSON by Spring Boot.
     */
    @PostMapping("/api/v1/parse/chapter")
    public ArchiveServerResponseData parseChapter(@RequestBody ArchiveServerRequestData request) {
        return archiveService.startParse(request, ArchiveParseType.CHAPTER);
    }

    /**
     * <p>
     *     This method accepts a story parsing request formatted as a {@link ArchiveServerRequestData} object
     *     and returns a confirmation that the request was accepted or rejected.
     * </p>
     * <p>
     *     Spring Boot attaches this method to the <code>/api/v1/parse/story</code> endpoint,
     *     creates an {@link ArchiveServerRequestData} object from the POST request body,
     *     and formats the response as JSON.
     * </p>
     * @param request The POST request body, formatted into an {@link ArchiveServerRequestData} object.
     * @return {@link ArchiveServerResponseData} that is formatted into JSON by Spring Boot.
     */
    @PostMapping("/api/v1/parse/story")
    public ArchiveServerResponseData parseStory(@RequestBody ArchiveServerRequestData request) {
        return archiveService.startParse(request, ArchiveParseType.STORY);
    }

    /**
     * <p>This method retrieves session information based on the session ID specified in the content-addressable URL.</p>
     * <p>Spring Boot attaches this method to the <code>/api/v1/parse/session/{sessionId}</code> endpoint.</p>
     * <p>
     *     Spring Boot pulls the session ID from the requested URL, passes it to the top-level archive service,
     *     and formats the response as JSON.
     * </p>
     * @param sessionId The session ID to retrieve information for.
     * @return {@link ArchiveServerResponseData} that is formatted into JSON by Spring Boot.
     */
    @GetMapping("/api/v1/parse/session/{sessionId}")
    public ArchiveServerResponseData getSessionInformation(@PathVariable String sessionId) {
        return archiveService.getSessionInformation(sessionId);
    }

    /**
     * <p>
     *     This method starts a live websocket feed that regularly publishes updates on a running parsing session.
     *     A confirmation response is sent back.
     * </p>
     * <p>The session that is chosen is dependent on the session ID specified in the content-addressable URL.</p>
     * <p>Spring Boot attaches this method to the <code>/api/v1/parse/session/{sessionId}/live</code> endpoint.</p>
     * <p>
     *     Spring Boot pulls the session ID from the requested URL, passes it to the top-level archive service,
     *     and formats the response as JSON.
     * </p>
     * @param sessionId The session ID to retrieve information for.
     * @return {@link ArchiveServerResponseData} that is formatted into JSON by Spring Boot.
     * @throws InterruptedException if the
     *      <code>Thread.sleep()</code> line in <code>ArchiveWebsocketService.runLiveSessionFeed()</code> is interrupted.
     */
    @GetMapping("/api/v1/parse/session/{sessionId}/live")
    public ArchiveServerResponseData getSessionInformationLive(@PathVariable String sessionId) throws InterruptedException {
        return archiveService.getSessionInformationLive(sessionId);
    }

    /**
     * <p>
     *     This method cancels a running session using the session ID specified in the content-addressable URL.
     *     A confirmation response is sent back.
     * </p>
     * <p>Spring Boot attaches this method to the <code>/api/v1/parse/session/{sessionId}/cancel</code> endpoint.</p>
     * <p>
     *     Spring Boot pulls the session ID from the requested URL, passes it to the top-level archive service,
     *     and formats the response as JSON.
     * </p>
     * @param sessionId The session ID to retrieve information for.
     * @return {@link ArchiveServerResponseData} that is formatted into JSON by Spring Boot.
     */
    @GetMapping("/api/v1/parse/session/{sessionId}/cancel")
    public ArchiveServerResponseData cancelSession(@PathVariable String sessionId) {
        return archiveService.cancelSession(sessionId);
    }
}
