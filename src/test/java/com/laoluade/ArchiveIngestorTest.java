package com.laoluade;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDate;

public class ArchiveIngestorTest {
    private static RemoteWebDriver testDriver;

    @BeforeAll
    public static void createDriver() throws URISyntaxException, MalformedURLException, SessionNotCreatedException {
        System.out.println("Creating test driver...");

        try {
            ChromeOptions options = new ChromeOptions();
            URI containerIdentifier = new URI("http://localhost:4444");
            URL containerLocator = containerIdentifier.toURL();
            testDriver = new RemoteWebDriver(containerLocator, options);
        }
        catch (URISyntaxException e) {
            System.out.println("URI creation failed.");
            Assertions.fail();
        }
        catch (MalformedURLException e) {
            System.out.println("URL creation failed.");
            Assertions.fail();
        }
        catch (SessionNotCreatedException e) {
            System.out.println("Session was unable to be created with container selenium. Check if a container is running.");
            Assertions.fail();
        }
    }

    @Test
    public void testSelenium() throws WebDriverException {
        try {
            testDriver.get("https://www.google.com/");
            System.out.println("Was able to access Google.");
        }
        catch (WebDriverException e) {
            System.out.println("Bad URL Link received. Check test link.");
            Assertions.fail();
        }
    }

    @Test
    public void testArchiveParse() throws InterruptedException, NoSuchElementException {
        try {
            // Parse the story page
            Chapter testChapter = ArchiveIngestor.createChapter(
                    testDriver, "test_series", "https://archiveofourown.org/works/45081343"
            );

            // Assert the title exists
            Assertions.assertNotNull(testChapter.pageTitle);
            Assertions.assertFalse(testChapter.pageTitle.isBlank());

            // Assert timestamp is good
            Assertions.assertInstanceOf(ZonedDateTime.class, testChapter.parentStory.timestamp);
            Assertions.assertEquals("UTC", testChapter.parentStory.timestamp.getZone().toString());

            // Assert the series is correct
            Assertions.assertEquals("test_series", testChapter.parentStory.series);

            // Assert the rating is right
            ArrayList<String> expectedRatings = new ArrayList<>(Arrays.asList(
                    "General Audiences", "Teen and Up", "Mature", "Explicit", "Not Rated"
            ));
            Assertions.assertEquals(1, testChapter.parentStory.ratingItems.size());
            Assertions.assertTrue(expectedRatings.contains(testChapter.parentStory.ratingItems.getFirst()));

            // Assert the warning items is good
            ArrayList<String> expectedWarnings = new ArrayList<>(Arrays.asList(
                    "Underage Sex", "Rape/Non-Con", "Graphic Depictions Of Violence", "Major Character Death",
                    "Creator Chose Not To Use Archive Warnings"
            ));
            for (String warningItem : testChapter.parentStory.warningItems) {
                Assertions.assertTrue(expectedWarnings.contains(warningItem));
            }

            // Assert the category items are good
            ArrayList<String> expectedCategories = new ArrayList<>(Arrays.asList(
                    "F/F", "F/M", "Gen", "M/M", "Multi", "Other"
            ));
            for (String categoryItem : testChapter.parentStory.categoryItems) {
                Assertions.assertTrue(expectedCategories.contains(categoryItem));
            }

            // Assert time statistics are good
            Assertions.assertInstanceOf(LocalDate.class, testChapter.parentStory.published);
            Assertions.assertInstanceOf(LocalDate.class, testChapter.parentStory.statusWhen);

            boolean publishBefore = testChapter.parentStory.published.isBefore(testChapter.parentStory.statusWhen);
            boolean publishEqual = testChapter.parentStory.published.isEqual(testChapter.parentStory.statusWhen);
            Assertions.assertTrue(publishBefore | publishEqual);

            ArrayList<String> expectedStatuses = new ArrayList<>(Arrays.asList(
                    "Completed", "Updated", ArchiveIngestor.PLACEHOLDER
            ));
            Assertions.assertTrue(expectedStatuses.contains(testChapter.parentStory.status));
        }
        catch (SessionNotCreatedException e) {
            System.out.println("Session was unable to be created with container selenium. Check if a container is running.");
            Assertions.fail();
        }
        catch (NoSuchElementException e) {
            System.out.println(e.getLocalizedMessage());
            Assertions.fail();
        }
    }

    @Test
    public void testSingleChapterParse() throws InterruptedException {
        // Parse the story page
        Chapter testChapter = ArchiveIngestor.createChapter(
                testDriver, "test_series", "https://archiveofourown.org/works/73057466"
        );
    }

    @AfterAll
    public static void quitDriver() {
        System.out.println("Quitting test driver...");
        testDriver.quit();
    }
}
