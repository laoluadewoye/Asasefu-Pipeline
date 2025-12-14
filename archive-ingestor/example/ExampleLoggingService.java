package com.laoluade.ingestor.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExampleLoggingService {
    private final Logger logger;

    public ExampleLoggingService() { this.logger = LoggerFactory.getLogger(ExampleLoggingService.class); }

    public void createLog(String logMessage) { this.logger.info(logMessage); }
}
