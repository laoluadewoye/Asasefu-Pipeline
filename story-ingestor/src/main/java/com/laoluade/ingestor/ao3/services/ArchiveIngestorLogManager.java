package com.laoluade.ingestor.ao3.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ArchiveIngestorLogManager {
    private final Logger archiveIngestorLogger;

    public ArchiveIngestorLogManager () {
        this.archiveIngestorLogger = LoggerFactory.getLogger(ArchiveIngestorLogManager.class);
    }

    public void createInfoLog(String log) { this.archiveIngestorLogger.info(log); }

    public void createErrorLog(String log) { this.archiveIngestorLogger.error(log); }
}
