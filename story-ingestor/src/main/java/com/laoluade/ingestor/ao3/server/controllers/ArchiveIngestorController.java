package com.laoluade.ingestor.ao3.server.controllers;

import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorRequest;
import com.laoluade.ingestor.ao3.server.models.ArchiveIngestorResponse;
import com.laoluade.ingestor.ao3.server.services.ArchiveIngestorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArchiveIngestorController {
    @Autowired
    ArchiveIngestorService archiveIngestorService;

    @GetMapping("/v1")
    public String Hello() {
        return "Hello World";
    }

    @GetMapping("/v1/parse/chapter")
    public ArchiveIngestorResponse parseChapter(@RequestBody ArchiveIngestorRequest request) {
        return archiveIngestorService.parseChapter(request);
    }

    @GetMapping("/v1/parse/story")
    public ArchiveIngestorResponse parseStory(@RequestBody ArchiveIngestorRequest request) {
        return archiveIngestorService.parseStory(request);
    }
}
