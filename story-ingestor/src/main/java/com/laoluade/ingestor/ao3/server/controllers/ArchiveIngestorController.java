package com.laoluade.ingestor.ao3.server.controllers;

import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorInfo;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorRequest;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorResponse;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorTestAPIInfo;
import com.laoluade.ingestor.ao3.server.services.ArchiveIngestorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArchiveIngestorController {
    @Autowired
    ArchiveIngestorService archiveIngestorService;

    // TODO: Update the test
    @GetMapping("/api/v1")
    public ArchiveIngestorTestAPIInfo getArchiveIngestorTestAPI() {
        String info = "Hello Archive Ingestor Version 1 API!";
        return new ArchiveIngestorTestAPIInfo(info);
    }

    @GetMapping("/api/v1/info")
    public ArchiveIngestorInfo getArchiveIngestorInfo() {
        return archiveIngestorService.getArchiveIngestorInfo();
    }

    @GetMapping("/api/v1/parse/chapter")
    public ArchiveIngestorResponse parseChapter(@RequestBody ArchiveIngestorRequest request) {
        return archiveIngestorService.parseChapter(request);
    }

    @GetMapping("/api/v1/parse/story")
    public ArchiveIngestorResponse parseStory(@RequestBody ArchiveIngestorRequest request) {
        return archiveIngestorService.parseStory(request);
    }
}
