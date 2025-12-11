package com.laoluade.ingestor.ao3.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ArchiveLogService {
    private final Logger archiveIngestorLogger;

    public ArchiveLogService() {
        this.archiveIngestorLogger = LoggerFactory.getLogger(ArchiveLogService.class);
    }

    public void createInfoLog(String log) { this.archiveIngestorLogger.info(log); }

    public void createErrorLog(String log) { this.archiveIngestorLogger.error(log); }
}
