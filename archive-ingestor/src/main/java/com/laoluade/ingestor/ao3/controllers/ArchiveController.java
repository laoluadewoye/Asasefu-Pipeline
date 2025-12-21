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

@RestController
public class ArchiveController {
    @Autowired
    ArchiveService archiveService;

    @GetMapping("/api/v1")
    public ArchiveServerTestData getArchiveIngestorTestData() {
        return archiveService.getArchiveIngestorTestData();
    }

    @GetMapping("/api/v1/spec")
    public ArchiveServerSpecData getArchiveServerSpecData() {
        return archiveService.getArchiveServerSpecData();
    }

    @PostMapping("/api/v1/parse/chapter")
    public ArchiveServerResponseData parseChapter(@RequestBody ArchiveServerRequestData request) {
        return archiveService.startParse(request, ArchiveParseType.CHAPTER);
    }

    @PostMapping("/api/v1/parse/story")
    public ArchiveServerResponseData parseStory(@RequestBody ArchiveServerRequestData request) {
        return archiveService.startParse(request, ArchiveParseType.STORY);
    }

    @GetMapping("/api/v1/parse/session/{sessionId}")
    public ArchiveServerResponseData getSessionInformation(@PathVariable String sessionId) {
        return archiveService.getSessionInformation(sessionId);
    }

    // TODO: Create a test for this
    @GetMapping("/api/v1/parse/session/{sessionId}/live")
    public ArchiveServerResponseData getSessionInformationLive(@PathVariable String sessionId) throws InterruptedException {
        return archiveService.getSessionInformationLive(sessionId);
    }

    @GetMapping("/api/v1/parse/session/{sessionId}/cancel")
    public ArchiveServerResponseData cancelSession(@PathVariable String sessionId) {
        return archiveService.cancelSession(sessionId);
    }
}
