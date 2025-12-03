package com.laoluade.ingestor.ao3.server.controllers;

// Server Packages
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorInfo;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorRequest;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorResponse;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorTestAPIInfo;
import com.laoluade.ingestor.ao3.server.services.ArchiveIngestorService;

// Spring Boot Packages
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArchiveIngestorController {
    @Autowired
    ArchiveIngestorService archiveIngestorService;

    @GetMapping("/api/v1")
    public ArchiveIngestorTestAPIInfo getArchiveIngestorTestAPI() {
        return archiveIngestorService.getArchiveIngestorTestAPI();
    }

    @GetMapping("/api/v1/info")
    public ArchiveIngestorInfo getArchiveIngestorInfo() {
        return archiveIngestorService.getArchiveIngestorInfo();
    }

    @GetMapping("/api/v1/parse/chapter")
    public ArchiveIngestorResponse parseChapter(@RequestBody ArchiveIngestorRequest request) {
        return archiveIngestorService.startParseChapter(request);
    }

    @GetMapping("/api/v1/parse/story")
    public ArchiveIngestorResponse parseStory(@RequestBody ArchiveIngestorRequest request) {
        return archiveIngestorService.startParseStory(request);
    }

    @GetMapping("/api/v1/parse/session/{sessionID}")
    public ArchiveIngestorResponse getSessionInformation(@PathVariable String sessionID) {
        return archiveIngestorService.getSessionInformation(sessionID);
    }

    @GetMapping("/api/v1/parse/session/{sessionID}/cancel")
    public ArchiveIngestorResponse cancelSession(@PathVariable String sessionID) {
        return archiveIngestorService.cancelSession(sessionID);
    }
}
