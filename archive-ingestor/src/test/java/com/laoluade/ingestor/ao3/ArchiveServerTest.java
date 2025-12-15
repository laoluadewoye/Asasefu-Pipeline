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
            "app-root", "app-header", "app-body", "app-tester", "app-footer"
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
                "otwarchive v0.9.449.0", testSpecInstance.getLatestOTWArchiveVersion(),
                "Mock Server returned incorrect OTWArchive version from /api/v1/spec."
        );
    }

    public void runResultTests(ArchiveServerResponseData response, String sessionId, boolean isInit,
                               boolean usesNickname) {
        // General assertions
        Assertions.assertFalse(sessionId.isEmpty(), "Response did not return a session ID.");
        Assertions.assertFalse(response.getResponseMessage().isEmpty(), "Response did not return a response message.");
        Assertions.assertEquals(sessionId, response.getSessionId(),
                "Session id setting for session " + sessionId + "'s initial response is not properly set."
        );

        // Init-dependent assertions
        if (isInit) {
            boolean isNewChapterMsg = response.getResponseMessage().equals(testMessageManager.getResponseNewChapterSession());
            boolean isNewStoryMsg = response.getResponseMessage().equals(testMessageManager.getResponseNewStorySession());
            Assertions.assertTrue(
                    isNewChapterMsg | isNewStoryMsg,
                    "Initial response for session " + sessionId +
                            " did not return the correct new session response."
            );
            Assertions.assertFalse(
                    response.isSessionFinished(),
                    "Is Finished setting for session " + sessionId + "'s initial response is not properly set."
            );
            Assertions.assertFalse(
                    response.isSessionCanceled(),
                    "Is Canceled setting for session " + sessionId + "'s initial response is not properly set."
            );
            Assertions.assertFalse(
                    response.isSessionException(),
                    "Is Exception setting for session " + sessionId + "'s initial response is not properly set."
            );
            Assertions.assertEquals(0, response.getParseChaptersCompleted(),
                    "Chapters Completed setting for session " + sessionId + "'s initial response is not properly set."
            );
            Assertions.assertEquals(0, response.getParseChaptersTotal(),
                    "Chapters Total setting for session " + sessionId + "'s initial response is not properly set."
            );
        }
        else {
            Assertions.assertFalse(
                    response.isSessionCanceled(),
                    "Is Canceled setting for session " + sessionId + "'s update response indicates something went wrong."
            );
            Assertions.assertFalse(
                    response.isSessionException(),
                    "Is Exception setting for session " + sessionId + "'s update response indicates something went wrong."
            );
            boolean totalChaptersUnknown = response.getParseChaptersTotal() == -1;
            boolean completedLessThanEqualToTotal = response.getParseChaptersCompleted() <= response.getParseChaptersTotal();
            Assertions.assertTrue(
                    totalChaptersUnknown | completedLessThanEqualToTotal,
                    "Completed chapters is greater than total chapters in a bad way for session " + sessionId + "."
            );
            if (!totalChaptersUnknown) {
                Assertions.assertTrue(
                        response.getParseChaptersTotal() >= 0,
                        "Total chapters is not at least zero in session " + sessionId + "."
                );
            }
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

        // Non-finished assertion
        if (!response.isSessionFinished()) {
            Assertions.assertTrue(
                    response.getParseResult().isEmpty(),
                    "Initial response for session " + sessionId + " returned something in parse result."
            );
        }
    }

    public void runSessionTest(MockMvc mvc, MvcResult initialResult, boolean usesNickname)
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
        boolean notFinished = true;
        while(notFinished) {
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

                // Try creating a JSON from the string
                JSONObject finalResultJSON = null;
                try {
                    finalResultJSON = new JSONObject(updateResponse.getParseResult());
                    System.out.println(finalResultJSON.toString(4));
                } catch (JSONException e) {
                    Assertions.fail("JSON string returned from parsing was unreadable.");
                }

                // Ensure that response facts line up with JSON facts
                JSONObject finalstoryInfo = null;
                try {
                    finalstoryInfo = finalResultJSON.getJSONObject("parentArchiveStoryInfo");
                }
                catch (JSONException e) {
                    finalstoryInfo = finalResultJSON.getJSONObject("archiveStoryInfo");
                }
                Assertions.assertEquals(
                        finalstoryInfo.getInt("currentChapters"), updateResponse.getParseChaptersCompleted(),
                        "Update response's completed chapters count does not line up with returned JSON."
                );
                Assertions.assertEquals(
                        finalstoryInfo.getInt("totalChapters"), updateResponse.getParseChaptersTotal(),
                        "Update response's total chapters count does not line up with returned JSON."
                );

                notFinished = false;
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
        runSessionTest(mvc, testInfoResult, usesNickname);
    }

    @Test
    public void testAPIParseCombinations(@Autowired MockMvc mvc) throws UnsupportedEncodingException,
            InterruptedException {
        // Run the chapter with nickname test
        String chapterTestLink = testLinks.getString("Chapter");
        testAPIParse(mvc, chapterTestLink, "/api/v1/parse/chapter", true);

        // Run the story without nickname test
        String storyTestLink = testLinks.getString("Story");
        testAPIParse(mvc, storyTestLink, "/api/v1/parse/story", false);
    }

    @Test
    public void testAPISessionCanceling(@Autowired MockMvc mvc) throws UnsupportedEncodingException {
        // Run the story without nickname test
        String storyTestLink = testLinks.getString("Story");
        ArchiveServerRequestData newRequest = new ArchiveServerRequestData(storyTestLink);
        String newRequestJSON = new ObjectMapper().writeValueAsString(newRequest);

        MvcResult testInitResult = null;
        try {
            testInitResult = mvc.perform(
                    post("/api/v1/parse/story").contentType(MediaType.APPLICATION_JSON).content(newRequestJSON).characterEncoding("utf-8")
            ).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to get request for /api/v1/parse/story.");
        }

        // Cancel the parsing midway
        String testInitString = testInitResult.getResponse().getContentAsString();
        ArchiveServerResponseData testInitResponse = new ObjectMapper().readValue(testInitString, ArchiveServerResponseData.class);
        String sessionId = testInitResponse.getSessionId();
        String sessionCancelingPath = "/api/v1/parse/session/" + sessionId + "/cancel";

        MvcResult testCancelResult = null;
        try {
            testCancelResult = mvc.perform(get(sessionCancelingPath))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to get request for " + sessionCancelingPath + ".");
        }

        String testCancelString = testCancelResult.getResponse().getContentAsString();
        ArchiveServerResponseData testCancelResponse = new ObjectMapper().readValue(testCancelString, ArchiveServerResponseData.class);
        Assertions.assertEquals(
                testMessageManager.getResponseCancelSucceeded(), testCancelResponse.getResponseMessage(),
                "Mock Server returned the wrong response message for canceling a session."
        );

        // Get an update
        sessionId = testCancelResponse.getSessionId();
        String sessionGettingPath = "/api/v1/parse/session/" + sessionId;

        MvcResult testUpdateResult = null;
        try {
            testUpdateResult = mvc.perform(get(sessionGettingPath))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to get request for " + sessionGettingPath + ".");
        }

        String testUpdateString = testUpdateResult.getResponse().getContentAsString();
        ArchiveServerResponseData testUpdateResponse = new ObjectMapper().readValue(testUpdateString, ArchiveServerResponseData.class);
        Assertions.assertTrue(
                testUpdateResponse.isSessionCanceled(),
                "Mock Server did not update the session cancel flag after canceling a session."
        );
        Assertions.assertFalse(
                testUpdateResponse.isSessionFinished(),
                "Mock Server updated the session finished flag after canceling a session."
        );
        Assertions.assertFalse(
                testUpdateResponse.isSessionException(),
                "Mock Server updated the session exception flag after canceling a session."
        );
    }

    @Test
    public void testInvalidSessionAccess (@Autowired MockMvc mvc) throws UnsupportedEncodingException {
        // Test session getting
        String sessionGettingPath = "/api/v1/parse/session/abc123";
        MvcResult testGetResult = null;
        try {
            testGetResult = mvc.perform(get(sessionGettingPath))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to get request for " + sessionGettingPath + ".");
        }
        String testGetString = testGetResult.getResponse().getContentAsString();
        ArchiveServerResponseData testGetResponse = new ObjectMapper().readValue(testGetString, ArchiveServerResponseData.class);
        Assertions.assertEquals(
                testMessageManager.getResponseGetSessionFailed(), testGetResponse.getResponseMessage(),
                "Mock server did not return the proper response for notifying that a session get request failed."
        );

        // Test session deleting
        String sessionCancelingPath = "/api/v1/parse/session/abc123/cancel";
        MvcResult testCancelResult = null;
        try {
            testCancelResult = mvc.perform(get(sessionCancelingPath))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to get request for " + sessionGettingPath + ".");
        }
        String testCancelString = testCancelResult.getResponse().getContentAsString();
        ArchiveServerResponseData testCancelResponse = new ObjectMapper().readValue(testCancelString, ArchiveServerResponseData.class);
        Assertions.assertEquals(
                testMessageManager.getResponseCancelFailed(), testCancelResponse.getResponseMessage(),
                "Mock server did not return the proper response for notifying that a session cancel request failed."
        );
    }

    @Test
    public void testBadPageLinkResponse (@Autowired MockMvc mvc) throws UnsupportedEncodingException,
            InterruptedException {
        // Run a chapter link on a story branch
        ArchiveServerRequestData newRequest = new ArchiveServerRequestData(
                "https://archiveofourown.org/works/1111/chapters/22222"
        );
        String newRequestJSON = new ObjectMapper().writeValueAsString(newRequest);

        MvcResult testInitResult = null;
        try {
            testInitResult = mvc.perform(
                    post("/api/v1/parse/story").contentType(MediaType.APPLICATION_JSON).content(newRequestJSON).characterEncoding("utf-8")
            ).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to post request for /api/v1/parse/chapter.");
        }

        String testInitString = testInitResult.getResponse().getContentAsString();
        ArchiveServerResponseData testInitResponse = new ObjectMapper().readValue(testInitString, ArchiveServerResponseData.class);
        Assertions.assertEquals(
                testMessageManager.getResponseBadStoryLink(), testInitResponse.getResponseMessage(),
                "Mock server did not return the proper response for notifying that a story service got a chapter link."
        );

        // Run the chapter with nickname test and bad ao3 URL
        newRequest = new ArchiveServerRequestData(
                "https://archiveofourown.org/works/999999999999", sessionNickname
        );
        newRequestJSON = new ObjectMapper().writeValueAsString(newRequest);

        testInitResult = null;
        try {
            testInitResult = mvc.perform(
                    post("/api/v1/parse/chapter").contentType(MediaType.APPLICATION_JSON).content(newRequestJSON).characterEncoding("utf-8")
            ).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to post request for /api/v1/parse/chapter.");
        }

        testInitString = testInitResult.getResponse().getContentAsString();
        testInitResponse = new ObjectMapper().readValue(testInitString, ArchiveServerResponseData.class);
        String sessionId = testInitResponse.getSessionId();

        // Get a final result
        Thread.sleep(11000);
        String sessionGettingPath = "/api/v1/parse/session/" + sessionId;
        MvcResult testUpdateResult = null;
        try {
            testUpdateResult = mvc.perform(get(sessionGettingPath))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to get request for " + sessionGettingPath + ".");
        }

        String testUpdateString = testUpdateResult.getResponse().getContentAsString();
        ArchiveServerResponseData testUpdateResponse = new ObjectMapper().readValue(testUpdateString, ArchiveServerResponseData.class);
//        Assertions.assertTrue(
//                testUpdateResponse.isSessionException(),
//                "Mock Server did not update the session exception flag after catching an exception during parsing."
//        );
        Assertions.assertFalse(
                testUpdateResponse.isSessionCanceled(),
                "Mock Server updated the session canceled flag after canceling a session."
        );
        Assertions.assertFalse(
                testUpdateResponse.isSessionFinished(),
                "Mock Server updated the session finished flag after canceling a session."
        );
        Assertions.assertTrue(
                testUpdateResponse.getParseResult().isEmpty(),
                "Mock server put something in parse result after canceling a session."
        );
        boolean messageIsURLError = testUpdateResponse.getResponseMessage().equals(
                testMessageManager.getResponseBadURLFormat()
        );
        boolean messageIsNotFound = testUpdateResponse.getResponseMessage().equals(
                testMessageManager.getLoggingErrorParseFailedNotFound()
        );
        Assertions.assertTrue(
                messageIsURLError | messageIsNotFound,
                "Mock server did not return the proper response for notifying of an element parsing failed."
        );

        // Run the chapter with a badly formatted link
        newRequest = new ArchiveServerRequestData("abc123def456");
        newRequestJSON = new ObjectMapper().writeValueAsString(newRequest);

        try {
            testInitResult = mvc.perform(
                    post("/api/v1/parse/chapter").contentType(MediaType.APPLICATION_JSON).content(newRequestJSON).characterEncoding("utf-8")
            ).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return proper error response to get request for /api/v1/parse/chapter.");
        }

        testInitString = testInitResult.getResponse().getContentAsString();
        testInitResponse = new ObjectMapper().readValue(testInitString, ArchiveServerResponseData.class);
        Assertions.assertEquals(
                testMessageManager.getResponseBadURLFormat(), testInitResponse.getResponseMessage(),
                "Mock server did not return the proper response for notifying of a badly formatted link."
        );
    }

    @Test
    public void testBadNicknameResponse (@Autowired MockMvc mvc) throws UnsupportedEncodingException {
        String chapterTestLink = testLinks.getString("Chapter");
        ArchiveServerRequestData newRequest = new ArchiveServerRequestData(
                chapterTestLink, "This nickname has spaces and quotes"
        );
        String newRequestJSON = new ObjectMapper().writeValueAsString(newRequest);

        MvcResult testInitResult = null;
        try {
            testInitResult = mvc.perform(
                    post("/api/v1/parse/chapter").contentType(MediaType.APPLICATION_JSON).content(newRequestJSON).characterEncoding("utf-8")
            ).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to get request for /api/v1/parse/chapter.");
        }

        String testInitString = testInitResult.getResponse().getContentAsString();
        ArchiveServerResponseData testInitResponse = new ObjectMapper().readValue(testInitString, ArchiveServerResponseData.class);
        Assertions.assertEquals(
                testMessageManager.getResponseBadNicknameFormat(), testInitResponse.getResponseMessage(),
                "Mock server did not return the proper response for notifying of a badly formatted nickname for chapter parsing."
        );

        newRequest = new ArchiveServerRequestData(
                chapterTestLink, "' OR '1'='1/cancel"
        );
        newRequestJSON = new ObjectMapper().writeValueAsString(newRequest);

        try {
            testInitResult = mvc.perform(
                    post("/api/v1/parse/story").contentType(MediaType.APPLICATION_JSON).content(newRequestJSON).characterEncoding("utf-8")
            ).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to get request for /api/v1/parse/chapter.");
        }

        testInitString = testInitResult.getResponse().getContentAsString();
        testInitResponse = new ObjectMapper().readValue(testInitString, ArchiveServerResponseData.class);
        Assertions.assertEquals(
                testMessageManager.getResponseBadNicknameFormat(), testInitResponse.getResponseMessage(),
                "Mock server did not return the proper response for notifying of a badly formatted nickname for story parsing."
        );
    }

    @Test
    public void testBadSessionIdResponse (@Autowired MockMvc mvc) throws UnsupportedEncodingException {
        // Try when getting session information
        MvcResult testGetResult = null;
        try {
            testGetResult = mvc.perform(get("/api/v1/parse/session/' OR '1'='1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to bad session ID for get request.");
        }
        String testGetString = testGetResult.getResponse().getContentAsString();
        ArchiveServerResponseData testGetResponse = new ObjectMapper().readValue(testGetString, ArchiveServerResponseData.class);
        Assertions.assertEquals(
                testMessageManager.getResponseBadSessionId(), testGetResponse.getResponseMessage(),
                "Mock server did not return the proper response for notifying of a badly formatted session id for get request."
        );
        
        // Try when canceling session
        MvcResult testCancelResult = null;
        try {
            testCancelResult = mvc.perform(get("/api/v1/parse/session/' OR '1'='1/cancel"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        } catch (Exception e) {
            Assertions.fail("Mock Server was unable to return response to bad session ID for get request.");
        }
        String testCancelString = testCancelResult.getResponse().getContentAsString();
        ArchiveServerResponseData testCancelResponse = new ObjectMapper().readValue(testCancelString, ArchiveServerResponseData.class);
        Assertions.assertEquals(
                testMessageManager.getResponseBadSessionId(), testCancelResponse.getResponseMessage(),
                "Mock server did not return the proper response for notifying of a badly formatted session id for cancel request."
        );
    }

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
