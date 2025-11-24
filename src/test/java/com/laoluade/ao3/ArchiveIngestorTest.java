package com.laoluade.ao3;

// JSON Packages
import org.json.JSONObject;

// JUnit Packages
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

// Selenium Packages
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

// I/O and URL Packages
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URISyntaxException;
import java.net.MalformedURLException;

// Datetime Packages
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.LocalDate;

// Structure Packages
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ArchiveIngestorTest {
    private static JSONObject testLinks;
    private static RemoteWebDriver testDriver;
    private static ArchiveIngestor testIngestor;
    private static final ArrayList<String> expectedRatings = new ArrayList<>(Arrays.asList(
            "General Audiences", "Teen And Up Audiences", "Mature", "Explicit", "Not Rated"
    ));
    private static final ArrayList<String> expectedWarnings = new ArrayList<>(Arrays.asList(
            "Underage Sex", "Rape/Non-Con", "Graphic Depictions Of Violence", "Major Character Death",
            "Creator Chose Not To Use Archive Warnings", "No Archive Warnings Apply"
    ));
    private static final ArrayList<String> expectedCategories = new ArrayList<>(Arrays.asList(
            "F/F", "F/M", "Gen", "M/M", "Multi", "Other"
    ));
    private static final ArrayList<String> expectedStatuses = new ArrayList<>(Arrays.asList(
            "Completed", "Updated", ArchiveIngestor.PLACEHOLDER
    ));

    @BeforeAll
    public static void setupTests() throws IOException, InterruptedException {
        System.out.println("Obtaining test links...");
        URL testLinksLocator = ArchiveIngestorTest.class.getResource("test_story_links.json");
        assert testLinksLocator != null;
        String testLinksPathDecoded = URLDecoder.decode(testLinksLocator.getPath(), StandardCharsets.UTF_8);
        testLinks = ArchiveIngestor.getJSONFromFilepath(testLinksPathDecoded);

        System.out.println("Creating archive ingestor instance...");
        testIngestor = new ArchiveIngestor();

        // TODO: Figure out a better way for this
//        System.out.println("Creating Selenium container...");
//        try {
//            String startSeleniumContainerCmd = "docker run -d -p 4444:4444 -p 7900:7900 --shm-size=\"2g\" " +
//                    "--name archive-ingestor-test-container selenium/standalone-chrome:136.0-20251101";
//            Runtime runtime = Runtime.getRuntime();
//            Process startProcess = runtime.exec(startSeleniumContainerCmd);
//            BufferedReader outputReader = new BufferedReader(new InputStreamReader(startProcess.getInputStream()));
//
//            String msg;
//            while((msg = outputReader.readLine()) != null) {
//                System.out.println(msg);
//            }
//            outputReader.close();
//        }
//        catch (IOException ioe) {
//            System.out.println(ioe.getMessage());
//            Assertions.fail();
//        }

        System.out.println("Waiting three seconds for container to fully start...");
        Thread.sleep(3000);

        System.out.println("Creating remote Selenium test driver...");
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
    public void testSelenium() {
        try {
            testDriver.get("https://www.google.com/");
            System.out.println("Was able to access Google.");
        }
        catch (WebDriverException e) {
            System.out.println("Bad URL Link received. Check test link.");
            Assertions.fail();
        }
    }

    public void runChapterParseTest(String pageLink) throws InterruptedException {
        try {
            // Parse the story page
            System.out.println("Opening website " + pageLink + "...");
            testDriver.get(pageLink);
            Chapter testChapter = testIngestor.createChapter(testDriver);

            // Assert timestamps are good
            Assertions.assertInstanceOf(ZonedDateTime.class, testChapter.parentStoryInfo.creationTimestamp);
            Assertions.assertEquals("UTC", testChapter.parentStoryInfo.creationTimestamp.getZone().toString());

            Assertions.assertInstanceOf(ZonedDateTime.class, testChapter.creationTimestamp);
            Assertions.assertEquals("UTC", testChapter.creationTimestamp.getZone().toString());

            // Assert that the pageLink supplied is valid

            // Assert that the paragraph list is not empty

            // Assert that the author list is not empty

            // Assert that the kudos math checks out
            // kudos = rk + urk + gk

            // Assert that the status doesn't have a colon

            // Assert the rating is right
            Assertions.assertEquals(1, testChapter.parentStoryInfo.ratings.size());
            Assertions.assertTrue(expectedRatings.contains(testChapter.parentStoryInfo.ratings.getFirst()));

            // Assert the warning items is good
            for (String warningItem : testChapter.parentStoryInfo.warnings) {
                Assertions.assertTrue(expectedWarnings.contains(warningItem));
            }

            // Assert the category items are good
            for (String categoryItem : testChapter.parentStoryInfo.categories) {
                Assertions.assertTrue(expectedCategories.contains(categoryItem));
            }

            // Assert time statistics are good
            Assertions.assertInstanceOf(LocalDate.class, testChapter.parentStoryInfo.published);
            Assertions.assertInstanceOf(LocalDate.class, testChapter.parentStoryInfo.statusWhen);

            boolean publishBefore = testChapter.parentStoryInfo.published.isBefore(testChapter.parentStoryInfo.statusWhen);
            boolean publishEqual = testChapter.parentStoryInfo.published.isEqual(testChapter.parentStoryInfo.statusWhen);
            Assertions.assertTrue(publishBefore | publishEqual);

            // Assert the status is good
            Assertions.assertTrue(expectedStatuses.contains(testChapter.parentStoryInfo.status));
            
            // Assert series items are good
            for (String series : testChapter.parentStoryInfo.series) {
                Assertions.assertFalse(series.contains("Part"));
            }
            
            // Assert numbers are good
            Assertions.assertTrue(testChapter.parentStoryInfo.words > -2);
            Assertions.assertTrue(testChapter.parentStoryInfo.currentChapters > -2);
            Assertions.assertTrue(testChapter.parentStoryInfo.totalChapters > -2);
            Assertions.assertTrue(testChapter.parentStoryInfo.comments > -2);
            Assertions.assertTrue(testChapter.parentStoryInfo.kudos > -2);
            Assertions.assertTrue(testChapter.parentStoryInfo.bookmarks > -2);
            Assertions.assertTrue(testChapter.parentStoryInfo.hits > -2);

            // Assert JSONs can return properly
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
        // Loop through the test set of stories
        Iterator<String> testLinksKeys = testLinks.keys();
        while (testLinksKeys.hasNext()) {
            String testStoryName = testLinksKeys.next();
            System.out.println("Current story: " + testStoryName);

            String testStoryLink = testLinks.getString(testStoryName);
            runChapterParseTest(testStoryLink);
        }
    }

    @Test
    public void testSingleStoryParse() throws InterruptedException {
        Iterator<String> testLinksKeys = testLinks.keys();
        String testStoryName = testLinksKeys.next();
        String testStoryLink = testLinks.getString(testStoryName);

        System.out.println("Opening website " + testStoryLink + "...");
        testDriver.get(testStoryLink);
        Story testStory = testIngestor.createStory(testDriver);
        System.out.println();

        // Call runChapterParseTest in a loop

        // Assert that there is no duplicate hashes

        // Assert that story title and chapter titles are distinct if multi

        // Assert that story title and chapter titles are the same if single

        // Assert that every story aside from Anna's Journal has comments
    }

    @AfterAll
    public static void quitDriver() {
        System.out.println("Closing remote Selenium test driver...");
        testDriver.quit();

//        System.out.println("Deleting Selenium container...");
//        try {
//            String removeSeleniumContainerCmd = "docker rm archive-ingestor-test-container";
//            Runtime runtime = Runtime.getRuntime();
//            Process startProcess = runtime.exec(removeSeleniumContainerCmd);
//            BufferedReader outputReader = new BufferedReader(new InputStreamReader(startProcess.getInputStream()));
//
//            String msg;
//            while((msg = outputReader.readLine()) != null) {
//                System.out.println(msg);
//            }
//            outputReader.close();
//        }
//        catch (IOException ioe) {
//            ioe.printStackTrace();
//            Assertions.fail();
//        }
    }
}
