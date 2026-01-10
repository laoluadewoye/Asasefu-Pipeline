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

/**
 * <p>This class is the Spring Boot service responsible for creating Selenium drivers.</p>
 * <p>This class uses the following settings from the application.properties file to configure itself:</p>
 * <ul>
 *     <li>archiveServer.driver.socket</li>
 *     <li>archiveServer.driver.timeoutSecs</li>
 * </ul>
 * <p>All <code>archiveServer.driver</code> settings have a class attribute counterpart.</p>
 */
@Service
public class ArchiveDriverService {
    /**
     * <p>This attribute represents the injected {@link ArchiveLogService}.</p>
     */
    // Service components
    @Autowired
    private final ArchiveLogService logService;

    /**
     * <p>This attribute represents the injected {@link ArchiveMessageService}.</p>
     */
    @Autowired
    private final ArchiveMessageService messageService;

    // Driver creation constants
    /**
     * <p>This attribute is the URL used to access the remote Selenium application.</p>
     */
    private final String driverSocket;

    /**
     * <p>This attribute is the time in seconds </p>
     */
    private final long timeoutSecs;

    /**
     * <p>This constructor injects services and values into the archive driver service.</p>
     * @param logService The injected logging service.
     * @param messageService The injected message service.
     * @param driverSocket The injected driver socket value.
     * @param timeoutSecs The injected timeout seconds value.
     */
    public ArchiveDriverService(ArchiveLogService logService, ArchiveMessageService messageService,
                                @Value("${archiveServer.driver.socket}") String driverSocket,
                                @Value("${archiveServer.driver.timeoutSecs}") long timeoutSecs) {
        this.logService = logService;
        this.messageService = messageService;
        this.driverSocket = driverSocket;
        this.timeoutSecs = timeoutSecs;

        this.logService.createInfoLog(this.messageService.createDriverSocketMessage(this.driverSocket));
    }

    /**
     * <p>This method asynchronously attempts to create a Selenium driver.</p>
     * @return A completable future that possibly contains a {@link RemoteWebDriver}.
     */
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

    /**
     * <p>This method returns either a fresh {@link RemoteWebDriver} or null if the attempt to create a driver fails.</p>
     * @return Either a fresh {@link RemoteWebDriver} or null.
     */
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
