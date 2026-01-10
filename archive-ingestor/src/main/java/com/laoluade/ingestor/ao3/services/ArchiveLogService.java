package com.laoluade.ingestor.ao3.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * <p>This class is the Spring Boot service responsible for logging messages.</p>
 */
@Service
public class ArchiveLogService {
    /**
     * <p>This attribute is the logger the service uses to create logs.</p>
     */
    private final Logger archiveIngestorLogger;

    /**
     * <p>This constructor initializes the service by creating a logger to use.</p>
     */
    public ArchiveLogService() {
        this.archiveIngestorLogger = LoggerFactory.getLogger(ArchiveLogService.class);
    }

    /**
     * <p>This method writes an info-level log to console.</p>
     * @param log The log to write.
     */
    public void createInfoLog(String log) { this.archiveIngestorLogger.info(log); }

    /**
     * <p>This method writes an error-level log to console.</p>
     * @param log The log to write.
     */
    public void createErrorLog(String log) { this.archiveIngestorLogger.error(log); }
}
