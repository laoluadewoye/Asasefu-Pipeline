package com.laoluade.ingestor.ao3.controllers;

// Server classes
import com.laoluade.ingestor.ao3.models.ArchiveServerSpecData;
import com.laoluade.ingestor.ao3.models.ArchiveServerRequestData;
import com.laoluade.ingestor.ao3.models.ArchiveServerResponseData;
import com.laoluade.ingestor.ao3.models.ArchiveServerTestData;
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
        return archiveService.startParseChapter(request);
    }

    @PostMapping("/api/v1/parse/story")
    public ArchiveServerResponseData parseStory(@RequestBody ArchiveServerRequestData request) {
        return archiveService.startParseStory(request);
    }

    @GetMapping("/api/v1/parse/session/{sessionID}")
    public ArchiveServerResponseData getSessionInformation(@PathVariable String sessionID) {
        return archiveService.getSessionInformation(sessionID);
    }

    @GetMapping("/api/v1/parse/session/{sessionID}/cancel")
    public ArchiveServerResponseData cancelSession(@PathVariable String sessionID) {
        return archiveService.cancelSession(sessionID);
    }
}
