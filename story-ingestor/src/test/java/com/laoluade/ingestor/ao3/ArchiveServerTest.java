package com.laoluade.ingestor.ao3;

// Server model classes
import com.laoluade.ingestor.ao3.core.ArchiveIngestor;
import com.laoluade.ingestor.ao3.models.ArchiveServerSpecData;
import com.laoluade.ingestor.ao3.models.ArchiveServerRequestData;
import com.laoluade.ingestor.ao3.models.ArchiveServerResponseData;
import com.laoluade.ingestor.ao3.models.ArchiveServerTestData;

// JUnit classes
import com.laoluade.ingestor.ao3.services.ArchiveMessageService;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

// Spring Boot header classes
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

// Spring boot function classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

// Spring boot assertion classes
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Core java classes
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@AutoConfigureMockMvc
public class ArchiveServerTest {
    // Test objects
    private static JSONObject testLinks;
    private static ArchiveMessageService testMessageManager;

    // Test values to check and use
    private static final ArrayList<String> expectedComponents = new ArrayList<>(Arrays.asList(
            "app-root", "app-header", "app-body", "app-testapi", "app-footer"
    ));
    private static final String sessionNickname = "testParseSession";
    private static final Integer sessionUpdateIntervalMilli = 2000;

    // Test runtime stuff
    private static final Runtime testRuntime = Runtime.getRuntime();
    private static final String[] runSeleniumContainerCmd = {
            "docker", "run", "-d", "-p", "4444:4444", "-p", "7900:7900", "--shm-size=\"2g\"",
            "--name", "archive-ingestor-test-container", "selenium/standalone-chrome:136.0-20251101"
    };
    private static final String[] stopSeleniumContainerCmd = {
            "docker", "stop", "archive-ingestor-test-container"
    };
    private static final String[] startSeleniumContainerCmd = {
            "docker", "start", "archive-ingestor-test-container"
    };
    private static final String[] removeSeleniumContainerCmd = {
            "docker", "rm", "archive-ingestor-test-container"
    };



    @BeforeAll
    public static void setupTests() throws IOException, InterruptedException {
        System.out.println("Obtaining test links...");
        URL testLinksLocator = ArchiveServerTest.class.getResource("test_story_links.json");
        assert testLinksLocator != null;
        String testLinksPathDecoded = URLDecoder.decode(testLinksLocator.getPath(), StandardCharsets.UTF_8);
        testLinks = ArchiveIngestor.getJSONFromFilepath(testLinksPathDecoded);

        System.out.println("Creating message manager...");
        testMessageManager = new ArchiveMessageService();

        System.out.println("Creating Selenium container...");
        try {
            Process runExec = testRuntime.exec(runSeleniumContainerCmd);
            int runExitCode = runExec.waitFor();
            if (runExitCode != 0) {
                Process stopExec = testRuntime.exec(stopSeleniumContainerCmd);
                stopExec.waitFor();
                Process startExec = testRuntime.exec(startSeleniumContainerCmd);
                startExec.waitFor();
            }
        }
        catch (IOException e) {
            Assertions.fail(e.toString());
        }

        System.out.println("Waiting five seconds for container to fully start...");
        Thread.sleep(5000);
    }

    @Test
    public void testIndex(@Autowired MockMvc mvc) throws UnsupportedEncodingException {
        // Return html
        MvcResult testIndex = null;
        try {
            testIndex = mvc.perform(get("/index.html"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.TEXT_HTML))
                    .andReturn();
        }
        catch (Exception e) {
            Assertions.fail("Mock Server was unable to return index.html.");
        }

        // Assert that all initial components are present in the html string
        Assertions.assertNotNull(testIndex, "testIndex variable is null.");
        String testIndexHTML = testIndex.getResponse().getContentAsString();
        for (String expectedComponent : expectedComponents) {
            Assertions.assertTrue(
                    testIndexHTML.contains(expectedComponent),
                    "index.html does not contain the component " + expectedComponent + "."
            );
        }
    }

    @Test
    public void testAPIBasic(@Autowired MockMvc mvc) throws UnsupportedEncodingException {
        // Test API version path
        MvcResult testBasicResult = null;
        try {
            testBasicResult = mvc.perform(get("/api/v1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        }
        catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to get request for /api/v1 (Test Data API).");
        }

        // Recreate the expected info model instance
        String testBasicString = testBasicResult.getResponse().getContentAsString();
        ArchiveServerTestData testTestInstance = new ObjectMapper().readValue(
                testBasicString, ArchiveServerTestData.class
        );
        Assertions.assertEquals(
                new ArchiveMessageService().getTestDataValue(), testTestInstance.getTestData(),
                "Mock Server returned incorrect information from /api/v1 (Test Data API)."
        );

        // Test backend info gathering
        try {
            testBasicResult = mvc.perform(get("/api/v1/spec"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to get request for /api/v1/spec.");
        }

        // Recreate the expected info model instance
        testBasicString = testBasicResult.getResponse().getContentAsString();
        ArchiveServerSpecData testSpecInstance = new ObjectMapper().readValue(testBasicString, ArchiveServerSpecData.class);

        Assertions.assertEquals(
                "0.1", testSpecInstance.getArchiveIngestorVersion(),
                "Mock Server returned incorrect archive ingestor version from /api/v1/spec."
        );
        Assertions.assertEquals(
                "otwarchive v0.9.447.1", testSpecInstance.getLatestOTWArchiveVersion(),
                "Mock Server returned incorrect OTWArchive version from /api/v1/spec."
        );
    }

    // TODO: Add more tests for the response object
    public void runResultTests(ArchiveServerResponseData response, String sessionId, boolean isInit,
                               boolean usesNickname) {
        // General assertions
        Assertions.assertFalse(sessionId.isEmpty(), "Response did not return a session ID.");

        // Init-dependent assertions
        if (isInit) {
            Assertions.assertEquals(
                    testMessageManager.getResponseNewChapterSession(), response.getResponseMessage(),
                    "Initial response for session " + sessionId +
                            " did not return the correct new chapter session response."
            );
            Assertions.assertTrue(
                    response.getParseResult().isEmpty(),
                    "Initial response for session " + sessionId + " returned something in parse result."
            );
            Assertions.assertFalse(
                    response.isSessionFinished(),
                    "Is Finished setting for session " + sessionId + "'s initial response is not properly set."
            );
            Assertions.assertFalse(
                    response.isSessionCanceled(),
                    "Is Canceled setting for session " + sessionId + "'s initial response is not properly set."
            );
        }
        else {
            Assertions.assertFalse(
                    response.isSessionCanceled(),
                    "Is Canceled setting for session " + sessionId +
                            "'s update response indicates something went wrong."
            );
        }

        // Nickname assertion
        if (usesNickname) {
            Assertions.assertEquals(
                    sessionNickname, response.getSessionNickname(),
                    "Nickname for session " + sessionId + " is not properly set."
            );
        }
        else {
            Assertions.assertEquals(
                    sessionId, response.getSessionNickname(),
                    "Nickname for session " + sessionId + " is not properly set to session ID by default."
            );
        }
    }

    public String runSessionTest(MockMvc mvc, MvcResult initialResult, boolean usesNickname)
            throws UnsupportedEncodingException, InterruptedException {
        // Parse initial result
        String initialResultString = initialResult.getResponse().getContentAsString();
        ArchiveServerResponseData initialResponse = new ObjectMapper().readValue(initialResultString, ArchiveServerResponseData.class);
        String sessionId = initialResponse.getSessionId();
        String sessionGettingPath = "/api/v1/parse/session/" + sessionId;

        // Analyze initial contents
        runResultTests(initialResponse, sessionId, true, usesNickname);

        // Start a cycle of update getting until a value is returned
        int updateCount = 1;
        while(true) {
            // Print the update count
            System.out.println("On update " + updateCount + " for session ID " + sessionId + "...");

            // Wait a bit before going again
            Thread.sleep(Duration.ofMillis(sessionUpdateIntervalMilli));

            // Get an update
            MvcResult updateResult = null;
            try {
                updateResult = mvc.perform(get(sessionGettingPath))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andReturn();
            } catch (Exception e) {
                Assertions.fail(
                        "Mock Server was unable to return response to get request for " + sessionGettingPath +
                                " on update " + updateCount + "."
                );
            }

            // Parse update result
            String updateResultString = updateResult.getResponse().getContentAsString();
            ArchiveServerResponseData updateResponse = new ObjectMapper().readValue(updateResultString, ArchiveServerResponseData.class);
            System.out.println(updateResponse.getResponseMessage());

            // Analyze the update
            runResultTests(updateResponse, sessionId, false, usesNickname);

            // Check if a proper end has been reached
            if (updateResponse.isSessionFinished()) {
                // Check that the string is there
                Assertions.assertFalse(
                        updateResponse.getParseResult().isEmpty(),
                        "Update response for session " + sessionId +
                                " did not return the JSON string on update " + updateCount +
                                " even though the finished flag is set."
                );
                return updateResponse.getParseResult();
            }

            // Update the update count
            updateCount++;
        }
    }

    public void testAPIParse(MockMvc mvc, String testLink, String parseURI, boolean usesNickname) throws
            UnsupportedEncodingException, InterruptedException {
        // Start the parsing
        String newRequestJSON;
        if (usesNickname) {
            ArchiveServerRequestData newRequest = new ArchiveServerRequestData(testLink, sessionNickname);
            newRequestJSON = new ObjectMapper().writeValueAsString(newRequest);
        }
        else {
            ArchiveServerRequestData newRequest = new ArchiveServerRequestData(testLink);
            newRequestJSON = new ObjectMapper().writeValueAsString(newRequest);
        }

        MvcResult testInfoResult = null;
        try {
            testInfoResult = mvc.perform(
                    post(parseURI).contentType(MediaType.APPLICATION_JSON).content(newRequestJSON).characterEncoding("utf-8")
            ).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to get request for " + parseURI + ".");
        }

        // Run the rest of the session testing
        String finalResultString = runSessionTest(mvc, testInfoResult, usesNickname);

        // Try creating a JSON from the string
        try {
            JSONObject finalResultJSON = new JSONObject(finalResultString);
            System.out.println(finalResultJSON.toString(4));
        } catch (JSONException e) {
            Assertions.fail("JSON string returned from parsing was unreadable.");
        }
    }

    @Test
    public void testAPIParseCombinations(@Autowired MockMvc mvc) throws UnsupportedEncodingException,
            InterruptedException {
        // Run the chapter with nickname test
        String chapterTestLink = testLinks.getString("Chapter");
        testAPIParse(mvc, chapterTestLink, "/api/v1/parse/chapter", true);

        // Run the chapter without nickname test
        testAPIParse(mvc, chapterTestLink, "/api/v1/parse/chapter", false);

        // Run the story with nickname test
        String storyTestLink = testLinks.getString("Story");
        testAPIParse(mvc, storyTestLink, "/api/v1/parse/story", true);

        // Run the story without nickname test
        testAPIParse(mvc, storyTestLink, "/api/v1/parse/story", false);
    }

    @Test
    public void testAPISessionCanceling(@Autowired MockMvc mvc) {
        // Cancel the parsing midway
    }

    @Test
    public void testResponseOutcomes(@Autowired MockMvc mvc) {
        // Outcomes
        //      - Session completes successfully
        //          * JSON-formatted json string
        //          * only finished flag is set
        //          * chapter count attributes are modified
        //      - Session is canceled
        //          * empty json string
        //          * only canceled flag is set
        //      - Session errors out (but properly)
        //          * empty json string
        //          * finished and canceled flags are both set
    }

    @Test
    public void testSessionInfoModification(@Autowired MockMvc mvc) {
        // Create a session info using two different ways and ensure it works expectedly
        // Assert the non modification of sessionInfo during instances where parsing is not taking place
        //      and a session is not be retrieved.
    }

    @Test
    public void testValidSessionAccess (@Autowired MockMvc mvc) {
        // Test both session info getting and session deleting
        // Test that once the session is deleted, it is unable to be retrieved.
        // Test that the session's place in session manager can expire on its own, and it is unable to be retrieved.
    }

    @Test
    public void testInvalidSessionAccess (@Autowired MockMvc mvc) {
        // Test both session info getting and session deleting
    }

    @Test
    public void testBadPageLinkResponse (@Autowired MockMvc mvc) {
        // startParse but a bad page link is sent to force interruption by exception catching.
        // startParse but a bad page link is sent to force interruption by URL checker.
    }

    // TODO: Check if a test configuration is needed for this
    @Test
    public void testAsyncTasks(@Autowired MockMvc mvc) {}

    @AfterAll
    public static void shutdownSelenium() throws InterruptedException {
        System.out.println("Deleting Selenium container...");
        try {
            Process stopExec = testRuntime.exec(stopSeleniumContainerCmd);
            stopExec.waitFor();
            Process removeExec = testRuntime.exec(removeSeleniumContainerCmd);
            removeExec.waitFor();
        }
        catch (IOException e) {
            Assertions.fail(e.toString());
        }
    }
}
