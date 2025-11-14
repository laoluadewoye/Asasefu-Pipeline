package com.laoluade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ArchiveInjestorTest {
    @Test
    public void testSelenium() throws SessionNotCreatedException, URISyntaxException, MalformedURLException {
        boolean testSuccess = true;

        try {
            // Create a remote driver
            ChromeOptions options = new ChromeOptions();
            URI containerIdentifier = new URI("http://localhost:4444");
            URL containerLocator = containerIdentifier.toURL();

            // Create driver
            RemoteWebDriver driver = new RemoteWebDriver(containerLocator, options);

            // Conduct the test
            driver.get("https://www.google.com/");
            driver.quit();
        }
        catch (SessionNotCreatedException e) {
            System.out.println("Session was unable to be created with container selenium. Check if a container is running.");
            testSuccess = false;
        }
        catch (URISyntaxException | MalformedURLException e) {
            System.out.println("URL creation failed.");
            testSuccess = false;
        }
        Assertions.assertTrue(testSuccess);
    }
}
