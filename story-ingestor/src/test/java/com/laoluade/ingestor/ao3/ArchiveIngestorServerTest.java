package com.laoluade.ingestor.ao3;

// Server model classes
import com.laoluade.ingestor.ao3.core.ArchiveIngestor;
import com.laoluade.ingestor.ao3.models.ArchiveIngestorInfo;
import com.laoluade.ingestor.ao3.models.ArchiveIngestorRequest;
import com.laoluade.ingestor.ao3.models.ArchiveIngestorResponse;
import com.laoluade.ingestor.ao3.models.ArchiveIngestorTestAPIInfo;

// JUnit classes
import com.laoluade.ingestor.ao3.services.ArchiveIngestorMessageManager;
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@AutoConfigureMockMvc
public class ArchiveIngestorServerTest {
    // Test objects
    private static JSONObject testLinks;
    private static ArchiveIngestorMessageManager testMessageManager;

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
        URL testLinksLocator = ArchiveIngestorServerTest.class.getResource("test_story_links.json");
        assert testLinksLocator != null;
        String testLinksPathDecoded = URLDecoder.decode(testLinksLocator.getPath(), StandardCharsets.UTF_8);
        testLinks = ArchiveIngestor.getJSONFromFilepath(testLinksPathDecoded);

        System.out.println("Creating message manager...");
        testMessageManager = new ArchiveIngestorMessageManager();

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
        MvcResult testInfoResult = null;
        try {
            testInfoResult = mvc.perform(get("/api/v1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        }
        catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to get request for /api/v1.");
        }

        // Recreate the expected info model instance
        String infoJSONString = testInfoResult.getResponse().getContentAsString();
        ArchiveIngestorTestAPIInfo testAPIInfoInstance = new ObjectMapper().readValue(
                infoJSONString, ArchiveIngestorTestAPIInfo.class
        );
        Assertions.assertEquals(
                "Hello Archive Ingestor Version 1 API!", testAPIInfoInstance.getInfo(),
                "Mock Server returned incorrect information from /api/v1."
        );

        // Test backend info gathering
        try {
            testInfoResult = mvc.perform(get("/api/v1/info"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to get request for /api/v1/info.");
        }

        // Recreate the expected info model instance
        infoJSONString = testInfoResult.getResponse().getContentAsString();
        ArchiveIngestorInfo testInfoInstance = new ObjectMapper().readValue(infoJSONString, ArchiveIngestorInfo.class);

        Assertions.assertEquals(
                "0.1", testInfoInstance.getArchiveIngestorVersion(),
                "Mock Server returned incorrect archive ingestor version from /api/v1/info."
        );
        Assertions.assertEquals(
                "otwarchive v0.9.446.1", testInfoInstance.getLatestOTWArchiveSupported(),
                "Mock Server returned incorrect OTWArchive version from /api/v1/info."
        );
    }

    public void runResultTests(ArchiveIngestorResponse response, String sessionID, boolean isInit,
                               boolean usesNickname) {
        // General assertions
        Assertions.assertFalse(sessionID.isEmpty(), "Response did not return a session ID.");
        Assertions.assertTrue(
                response.getSessionInfo().getCreationTimestamp().contains("UTC"),
                "Creation time stamp for session " + sessionID + " is not in UTC time."
        );

        // Init-dependent assertions
        if (isInit) {
            Assertions.assertEquals(
                    testMessageManager.getResponseNewChapterSession(), response.getResultMessage(),
                    "Initial response for session " + sessionID +
                            " did not return the correct new chapter session response."
            );
            Assertions.assertTrue(
                    response.getResponseJSONString().isEmpty(),
                    "Initial response for session " + sessionID + " returned something in response JSON attribute."
            );
            Assertions.assertFalse(
                    response.getSessionInfo().getIsFinished(),
                    "Is Finished setting for session " + sessionID + "'s initial response is not properly set."
            );
            Assertions.assertFalse(
                    response.getSessionInfo().getIsCanceled(),
                    "Is Canceled setting for session " + sessionID + "'s initial response is not properly set."
            );
        }
        else {
            Assertions.assertFalse(
                    response.getSessionInfo().getIsCanceled(),
                    "Is Canceled setting for session " + sessionID +
                            "'s update response indicates something went wrong."
            );
        }

        // Nickname assertion
        if (usesNickname) {
            Assertions.assertEquals(
                    sessionNickname, response.getSessionInfo().getSessionNickname(),
                    "Nickname for session " + sessionID + " is not properly set."
            );
        }
        else {
            Assertions.assertEquals(
                    sessionID, response.getSessionInfo().getSessionNickname(),
                    "Nickname for session " + sessionID + " is not properly set to session ID by default."
            );
        }
    }

    public String runSessionTest(MockMvc mvc, MvcResult initialResult, boolean usesNickname) throws UnsupportedEncodingException,
            InterruptedException {
        // Parse initial result
        String initialResultString = initialResult.getResponse().getContentAsString();
        ArchiveIngestorResponse initialResponse = new ObjectMapper().readValue(initialResultString, ArchiveIngestorResponse.class);
        String sessionID = initialResponse.getSessionInfo().getSessionID();
        String sessionGettingPath = "/api/v1/parse/session/" + sessionID;

        // Analyze initial contents
        runResultTests(initialResponse, sessionID, true, usesNickname);

        // Start a cycle of update getting until a value is returned
        int updateCount = 1;
        while(true) {
            // Print the update count
            System.out.println("On update " + updateCount + " for session ID " + sessionID + "...");

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
            ArchiveIngestorResponse updateResponse = new ObjectMapper().readValue(updateResultString, ArchiveIngestorResponse.class);
            System.out.println(updateResponse.getSessionInfo().getLastMessage());

            // Analyze the update
            runResultTests(updateResponse, sessionID, false, usesNickname);

            // Check if a proper end has been reached
            if (updateResponse.getSessionInfo().getIsFinished()) {
                // Check that the string is there
                Assertions.assertFalse(
                        initialResponse.getResponseJSONString().isEmpty(),
                        "Update response for session " + sessionID +
                                " did not return the JSON string on update " + updateCount +
                                " even though the finished flag is set."
                );
                return initialResponse.getResponseJSONString();
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
            ArchiveIngestorRequest newRequest = new ArchiveIngestorRequest(testLink, sessionNickname);
            newRequestJSON = new ObjectMapper().writeValueAsString(newRequest);
        }
        else {
            ArchiveIngestorRequest newRequest = new ArchiveIngestorRequest(testLink);
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
