package com.laoluade.ingestor.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ExampleMonitoringService {
    @Autowired
    private final ExampleLoggingService loggingService;

    private final ExampleNumber currentExampleNumber;

    public ExampleMonitoringService(ExampleLoggingService loggingService) {
        this.loggingService = loggingService;
        this.currentExampleNumber = new ExampleNumber(0);
        this.loggingService.createLog("Set current example number to 0");
    }

    public synchronized void setExample(Integer newNumber) {
        this.currentExampleNumber.setNumber(newNumber);
        this.loggingService.createLog("Set current example number to " + newNumber);
    }

    public synchronized ExampleNumber getExample() {
        this.loggingService.createLog(
                "Returning current example number of " + this.currentExampleNumber.getNumber()
        );
        return this.currentExampleNumber;
    }

    @Async("exampleAsyncExecutor")
    public void monitoringLoop() throws InterruptedException {
        while (true) {
            Thread.sleep(2000);
            this.loggingService.createLog(
                    "Monitoring current example number of " + this.currentExampleNumber.getNumber()
            );
        }
    }
}
