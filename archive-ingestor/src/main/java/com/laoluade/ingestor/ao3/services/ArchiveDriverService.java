package com.laoluade.ingestor.ao3.services;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ArchiveDriverService {
    // Service components
    @Autowired
    private final ArchiveLogService logService;

    @Autowired
    private final ArchiveMessageService messageService;

    // Driver creation constants
    private final String driverSocket;
    private final long timeoutSecs;

    public ArchiveDriverService(ArchiveLogService logService, ArchiveMessageService messageService,
                                @Value("${archiveServer.driver.socket}") String driverSocket,
                                @Value("${archiveServer.driver.timeoutSecs}") long timeoutSecs) {
        this.logService = logService;
        this.messageService = messageService;
        this.driverSocket = driverSocket;
        this.timeoutSecs = timeoutSecs;

        this.logService.createInfoLog(this.messageService.createDriverSocketMessage(this.driverSocket));
    }

    public CompletableFuture<RemoteWebDriver> createDriver() {
        return CompletableFuture.supplyAsync(() -> {
            URL containerLocator;
            try {
                URI containerIdentifier = new URI(this.driverSocket);
                containerLocator = containerIdentifier.toURL();
            }
            catch (URISyntaxException | MalformedURLException e) {
                this.logService.createErrorLog(this.messageService.createURLExceptionMessage(this.driverSocket));
                return null;
            }

            RemoteWebDriver driver = new RemoteWebDriver(containerLocator, new ChromeOptions());
            this.logService.createInfoLog(this.messageService.getLoggingInfoCreatedDriver());
            return driver;
        });
    }

    public RemoteWebDriver obtainDriverOrNull() {
        try {
            CompletableFuture<RemoteWebDriver> possibleDriver = createDriver();
            possibleDriver.completeOnTimeout(null, this.timeoutSecs, TimeUnit.SECONDS);
            possibleDriver.join();
            return possibleDriver.get();
        }
        catch (Exception e) {
            return null;
        }
    }
}
