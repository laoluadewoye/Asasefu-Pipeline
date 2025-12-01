package com.laoluade.ingestor.ao3.server;

// JUnit classes
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorInfo;
import org.junit.jupiter.api.Assertions;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@AutoConfigureMockMvc
public class ArchiveIngestorServerTest {
    @Test
    void testIndex(@Autowired MockMvc mvc) throws Exception {
        MvcResult testIndex = mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andReturn();

        String indexString = testIndex.getResponse().getContentAsString();
        System.out.println();
    }

    @Test
    void testAPI(@Autowired MockMvc mvc) throws Exception {
        // Test API version path
        mvc.perform(get("/api/v1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello Archive Ingestor Service Version 1 API!"));

        // Test backend info gathering
        MvcResult testInfoResult = mvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Recreate the expected info model instance
        String infoJSONString = testInfoResult.getResponse().getContentAsString();
        ArchiveIngestorInfo testInfoInstance = new ObjectMapper().readValue(infoJSONString, ArchiveIngestorInfo.class);

        Assertions.assertEquals("0.1", testInfoInstance.getArchiveIngestorVersion());
        Assertions.assertEquals("otwarchive v0.9.445.0", testInfoInstance.getLatestOTWArchiveSupported());
    }
}
