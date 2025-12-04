package com.laoluade.ingestor.ao3.server;

// Server model classes
import com.laoluade.ingestor.ao3.core.ArchiveIngestor;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorInfo;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorTestAPIInfo;

// JUnit classes
import org.json.JSONObject;
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
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO: Backend is pretty much built now, so add some tests!
@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@AutoConfigureMockMvc
public class ArchiveIngestorServerTest {
    private static JSONObject testLinks;

    @BeforeAll
    public static void setupTests() throws IOException {
        System.out.println("Obtaining test links...");
        URL testLinksLocator = ArchiveIngestorServerTest.class.getResource("test_story_links.json");
        assert testLinksLocator != null;
        String testLinksPathDecoded = URLDecoder.decode(testLinksLocator.getPath(), StandardCharsets.UTF_8);
        testLinks = ArchiveIngestor.getJSONFromFilepath(testLinksPathDecoded);
    }

    @Test
    void testIndex(@Autowired MockMvc mvc) throws Exception {
        MvcResult testIndex = mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andReturn();
    }

    @Test
    void testAPIBasic(@Autowired MockMvc mvc) throws Exception {
        // Test API version path
        MvcResult testInfoResult = mvc.perform(get("/api/v1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Recreate the expected info model instance
        String infoJSONString = testInfoResult.getResponse().getContentAsString();
        ArchiveIngestorTestAPIInfo testAPIInfoInstance = new ObjectMapper().readValue(
                infoJSONString, ArchiveIngestorTestAPIInfo.class
        );

        Assertions.assertEquals("Hello Archive Ingestor Version 1 API!", testAPIInfoInstance.getInfo());

        // Test backend info gathering
        testInfoResult = mvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Recreate the expected info model instance
        infoJSONString = testInfoResult.getResponse().getContentAsString();
        ArchiveIngestorInfo testInfoInstance = new ObjectMapper().readValue(infoJSONString, ArchiveIngestorInfo.class);

        Assertions.assertEquals("0.1", testInfoInstance.getArchiveIngestorVersion());
        Assertions.assertEquals("otwarchive v0.9.446.1", testInfoInstance.getLatestOTWArchiveSupported());
    }

    @Test
    void testAPIParseChapterWithNickname(@Autowired MockMvc mvc) throws Exception {
        JSONObject chapterTestLink = testLinks.getJSONObject("Chapter");
        String jsonPayload = "{\"pagelinkURL\": " + chapterTestLink +  ", \"sessionNickname\": \"testParseSession\"}";

        MvcResult testInfoResult = mvc.perform(
                get("/api/v1/parse/chapter").contentType(MediaType.APPLICATION_JSON).content(jsonPayload)
        ).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}

/*

* Scenarios to assert

* startParse but no nickname is supplied (chapter and story)
* startParse but nickname is supplied (chapter and story)
* Assert the non modification of sessionInfo during instances where parsing is not taking place and a session is not be retrieved.
* Session is attempted to be retrieved or deleted but doesn't exist.
* Session is attempted to be retrieved or deleted.
* startParse but a bad pagelink is sent to force interruption by exception catching.

*/
