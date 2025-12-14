package com.laoluade.ingestor.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExampleService {
    @Autowired
    private final ExampleAsyncService asyncService;

    @Autowired
    private final ExampleMonitoringService monitoringService;

    public ExampleService(ExampleAsyncService asyncService, ExampleMonitoringService monitoringService)
            throws InterruptedException {
        this.asyncService = asyncService;
        this.monitoringService = monitoringService;
        this.monitoringService.monitoringLoop();
    }

    public ExampleNumber startAsyncService() throws InterruptedException {
        this.asyncService.incrementLoop();
        return new ExampleNumber(-1);
    }

    public ExampleNumber getExample() {
        return this.monitoringService.getExample();
    }
}
