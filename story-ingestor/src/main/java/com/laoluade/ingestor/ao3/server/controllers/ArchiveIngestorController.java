package com.laoluade.ingestor.ao3.server.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArchiveIngestorController {
    @GetMapping("/")
    public String Hello() {
        return "Hello World";
    }
}
