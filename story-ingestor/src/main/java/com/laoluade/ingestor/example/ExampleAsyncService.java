package com.laoluade.ingestor.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ExampleAsyncService {
    @Autowired
    private final ExampleLoggingService loggingService;

    @Autowired
    private final ExampleMonitoringService monitoringService;

    public ExampleAsyncService(ExampleLoggingService loggingService, ExampleMonitoringService monitoringService) {
        this.loggingService = loggingService;
        this.monitoringService = monitoringService;
    }

    public void updateExample(Integer asyncNumber) {
        this.monitoringService.setExample(asyncNumber);
    }

    @Async("exampleAsyncExecutor")
    public CompletableFuture<Integer> incrementLoop() throws InterruptedException {
        Integer asyncNumber = 0;

        while (true) {
            Thread.sleep(500);
            asyncNumber++;
            this.loggingService.createLog("Async Incremented number to " + asyncNumber);
            this.updateExample(asyncNumber);
        }
    }
}
