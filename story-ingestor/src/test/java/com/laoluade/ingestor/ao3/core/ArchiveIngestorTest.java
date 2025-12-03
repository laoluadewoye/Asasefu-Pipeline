package com.laoluade.ingestor.ao3.core;

// Core Error Package
import com.laoluade.ingestor.ao3.errors.*;

// JSON Packages
import org.json.JSONException;
import org.json.JSONObject;

// JUnit Packages
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

// Selenium Packages
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

// I/O and URL Packages
import java.io.IOException;
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
import java.lang.reflect.Field;

public class ArchiveIngestorTest {
    // Test objects
    private static JSONObject testLinks;
    private static RemoteWebDriver testDriver;
    private static ArchiveIngestor testIngestor;

    // Test values to check
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

    // Test runtime stuff
    private static final Runtime testRuntime = Runtime.getRuntime();
    private static final String[] runSeleniumContainerCmd = {
            "docker", "run", "-d", "-p", "4444:4444", "-p", "7900:7900", "--shm-size=\"2g\"",
            "--name", "archive-ingestor-test-container", "selenium/standalone-chrome:136.0-20251101"
    };
    private static final String[] stopSeleniumContainerCmd = {
            "docker", "stop", "archive-ingestor-test-container"
    };
    private static final String[] startSeleniumContainerCmd = {
            "docker", "start", "archive-ingestor-test-container"
    };
    private static final String[] removeSeleniumContainerCmd = {
            "docker", "rm", "archive-ingestor-test-container"
    };

    @BeforeAll
    public static void setupTests() throws IOException, InterruptedException {
        System.out.println("Obtaining test links...");
        URL testLinksLocator = ArchiveIngestorTest.class.getResource("test_story_links.json");
        assert testLinksLocator != null;
        String testLinksPathDecoded = URLDecoder.decode(testLinksLocator.getPath(), StandardCharsets.UTF_8);
        testLinks = ArchiveIngestor.getJSONFromFilepath(testLinksPathDecoded);

        System.out.println("Creating archive ingestor instance...");
        testIngestor = new ArchiveIngestor();

        System.out.println("Creating Selenium container...");
        try {
            Process runExec = testRuntime.exec(runSeleniumContainerCmd);
            int runExitCode = runExec.waitFor();
            if (runExitCode != 0) {
                Process stopExec = testRuntime.exec(stopSeleniumContainerCmd);
                stopExec.waitFor();
                Process startExec = testRuntime.exec(startSeleniumContainerCmd);
                startExec.waitFor();
            }
        }
        catch (IOException e) {
            Assertions.fail(e.toString());
        }

        System.out.println("Waiting five seconds for container to fully start...");
        Thread.sleep(5000);

        System.out.println("Creating remote Selenium test driver...");
        try {
            ChromeOptions options = new ChromeOptions();
            URI containerIdentifier = new URI("http://localhost:4444");
            URL containerLocator = containerIdentifier.toURL();
            testDriver = new RemoteWebDriver(containerLocator, options);
        }
        catch (URISyntaxException e) {
            Assertions.fail("URI creation failed.");
        }
        catch (MalformedURLException e) {
            Assertions.fail("URL creation failed.");
        }
        catch (SessionNotCreatedException e) {
            Assertions.fail(
                    "Session was unable to be created with container selenium. Check if Docker is installed."
            );
        }
    }

    @Test
    public void testSelenium() {
        try {
            testDriver.get("https://www.google.com/");
            System.out.println("Was able to access Google.");
        }
        catch (WebDriverException e) {
            Assertions.fail("Bad URL Link received. Check test link.");
        }
    }

    @Test
    public void testBadWork() {
        try {
            testDriver.get("https://archiveofourown.org/works/XXXXXXXX");
            testIngestor.createChapter(testDriver);
        }
        catch (ArchiveVersionIncompatibleError | ChapterContentNotFoundError | IngestorCanceledError |
               IngestorElementNotFoundError e) {
            System.out.println(e.toString());
        }
        catch (InterruptedException e) {
            Assertions.fail("For some reason the thread threw this exception.");
        }
        catch (Exception e) {
            Assertions.fail("Intentional bad link broke the ingestor in an unintentional way.");
        }
    }

    @Test
    public void testArchiveVersion() {
        testDriver.get("https://archiveofourown.org/works/XXXXXXXX");
        try {
            testIngestor.checkArchiveVersion(testDriver);
        } catch (ArchiveVersionIncompatibleError e) {
            Assertions.fail("Archive version is out of date. This is fine if it happens post-release but not here.");
        }
    }

    public void runChapterTests(Chapter testChapter, String storyCreationTimestamp) {
        // Assert chapter timestamps are good
        Assertions.assertInstanceOf(
                ZonedDateTime.class, testChapter.creationTimestamp,
                "Chapter " + testChapter.chapterTitle + " timestamp is not a ZonedDateTime object."
        );
        Assertions.assertEquals(
                "UTC", testChapter.creationTimestamp.getZone().toString(),
                "Chapter " + testChapter.chapterTitle + " timestamp is not set to UTC."
        );
        Assertions.assertNotEquals(
                testChapter.parentStoryInfo.creationTimestamp.toString(), testChapter.creationTimestamp.toString(),
                "Chapter " + testChapter.chapterTitle + " timestamp is the same as the parent story info timestamp."
        );
        Assertions.assertNotEquals(
                storyCreationTimestamp, testChapter.creationTimestamp.toString(),
                "Chapter " + testChapter.chapterTitle + " timestamp is the same as the parent story timestamp."
        );

        // Assert that the pageLink supplied is valid
        try {
            URI testURI = new URI(testChapter.pageLink);
            URL testURL = testURI.toURL();
        }
        catch (MalformedURLException | URISyntaxException e) {
            Assertions.fail("Chapter " + testChapter.chapterTitle + " page link is bad.");
        }

        // Assert that the paragraph list is not empty
        Assertions.assertNotEquals(
                0, testChapter.paragraphs.size(),
                "Chapter " + testChapter.chapterTitle + " does not contain the actual chapter."
        );

        // Assert JSONs can return properly
        JSONObject testJSONFatherful = testChapter.getJSONRepWithParent();
        JSONObject testJSONFatherless = testChapter.getJSONRepWithoutParent();

        Assertions.assertInstanceOf(
                JSONObject.class, testJSONFatherful,
                "Chapter " + testChapter.chapterTitle +
                        " JSON representation with parent is not a JSONObject object."
        );
        Assertions.assertInstanceOf(
                JSONObject.class, testJSONFatherless,
                "Chapter " + testChapter.chapterTitle +
                        " JSON representation without parent is not a JSONObject object."
        );

        // Assert JSON contains all values
        try {
            for (Field field : Chapter.class.getDeclaredFields()) {
                if (field.getName().equals("parentStoryInfo")) {
                    JSONObject psi = testJSONFatherful.getJSONObject(field.getName());
                    for (Field innerField : StoryInfo.class.getDeclaredFields()) {
                        if (!innerField.getName().equals("isSet")) {
                            psi.get(innerField.getName());
                        }
                    }
                }
                else {
                    testJSONFatherful.get(field.getName());
                    testJSONFatherless.get(field.getName());
                }
            }
        }
        catch (JSONException e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testChapterParse() throws InterruptedException, ChapterContentNotFoundError, IngestorCanceledError,
            IngestorElementNotFoundError {
        JSONObject chapterTestLinks = testLinks.getJSONObject("Chapter");
        Iterator<String> testLinksKeys = chapterTestLinks.keys();
        while (testLinksKeys.hasNext()) {
            // Parse the chapter
            String testStoryName = testLinksKeys.next();
            String testStoryLink = chapterTestLinks.getString(testStoryName);
            testDriver.get(testStoryLink);
            Chapter testChapter = testIngestor.createChapter(testDriver);

            // Test the chapter
            runChapterTests(testChapter, testChapter.parentStoryInfo.creationTimestamp.toString());
        }
    }

    public void runStoryTests(Story testStory) {
        // Assert story timestamps are good
        Assertions.assertInstanceOf(
                ZonedDateTime.class, testStory.creationTimestamp,
                "Story timestamp is not a ZonedDateTime object."
        );
        Assertions.assertEquals(
                "UTC", testStory.creationTimestamp.getZone().toString(),
                "Story timestamp is not set to UTC."
        );
        Assertions.assertInstanceOf(
                ZonedDateTime.class, testStory.storyInfo.creationTimestamp,
                "Story info timestamp is not a ZonedDateTime object."
        );
        Assertions.assertEquals(
                "UTC", testStory.storyInfo.creationTimestamp.getZone().toString(),
                "Story info timestamp is not set to UTC."
        );
        Assertions.assertNotEquals(
                testStory.creationTimestamp.toString(), testStory.storyInfo.toString(),
                "Story " + testStory.storyInfo.title + " timestamp is the same as the story info timestamp."
        );

        // Assert that the author list is not empty
        Assertions.assertNotEquals(
                0, testStory.storyInfo.authors.size(),
                "Story " + testStory.storyInfo.title + " does not contain the author(s)."
        );

        // Assert that the kudos math checks out
        Integer totalKudos = testStory.storyInfo.registeredKudos.size() +
                testStory.storyInfo.unnamedRegisteredKudos +
                testStory.storyInfo.guestKudos;
        Assertions.assertEquals(
                testStory.storyInfo.kudos, totalKudos,
                "Story " + testStory.storyInfo.title + " kudos calculations are off."
        );

        // Assert that the status doesn't have a colon
        Assertions.assertFalse(
                testStory.storyInfo.status.contains(":"),
                "Story " + testStory.storyInfo.title + " status contains a colon."
        );

        // Assert the rating is right
        Assertions.assertEquals(
                1, testStory.storyInfo.ratings.size(),
                "Story " + testStory.storyInfo.title + " contains more than one rating."
        );
        Assertions.assertTrue(expectedRatings.contains(
                        testStory.storyInfo.ratings.getFirst()),
                "Story " + testStory.storyInfo.title + " rating is invalid."
        );

        // Assert the warning items is good
        for (String warningItem : testStory.storyInfo.warnings) {
            Assertions.assertTrue(
                    expectedWarnings.contains(warningItem),
                    "Story " + testStory.storyInfo.title + " warning " + warningItem + " is invalid."
            );
        }

        // Assert the category items are good
        for (String categoryItem : testStory.storyInfo.categories) {
            Assertions.assertTrue(
                    expectedCategories.contains(categoryItem),
                    "Story " + testStory.storyInfo.title + " category " + categoryItem + " is invalid."
            );
        }

        // Assert time statistics are good
        Assertions.assertInstanceOf(
                LocalDate.class, testStory.storyInfo.published,
                "Story " + testStory.storyInfo.title +
                        " parent story info publish date is not a LocalDate Object."
        );
        Assertions.assertInstanceOf(
                LocalDate.class, testStory.storyInfo.statusWhen,
                "Story " + testStory.storyInfo.title +
                        " parent story info status date is not a LocalDate Object."
        );

        boolean publishBefore = testStory.storyInfo.published.isBefore(testStory.storyInfo.statusWhen);
        boolean publishEqual = testStory.storyInfo.published.isEqual(testStory.storyInfo.statusWhen);
        Assertions.assertTrue(
                publishBefore | publishEqual,
                "Story " + testStory.storyInfo.title +
                        " parent story info publish date is after the updated/completed date."
        );

        // Assert the status is good
        Assertions.assertTrue(
                expectedStatuses.contains(testStory.storyInfo.status),
                "Story " + testStory.storyInfo.title + " parent story info status is invalid."
        );

        // Assert series items are good
        for (String series : testStory.storyInfo.series) {
            Assertions.assertFalse(
                    series.contains("Part"),
                    "Story " + testStory.storyInfo.title + " parent story series " + series + " is invalid."
            );
        }

        // Assert numbers are good
        Assertions.assertTrue(
                testStory.storyInfo.words > -2,
                "Story " + testStory.storyInfo.title + " parent story info word count is invalid."
        );
        Assertions.assertTrue(
                testStory.storyInfo.currentChapters > -2,
                "Story " + testStory.storyInfo.title + " parent story info current chapter count is invalid."
        );
        Assertions.assertTrue(
                testStory.storyInfo.totalChapters > -2,
                "Story " + testStory.storyInfo.title + " parent story info total chapter count is invalid."
        );
        Assertions.assertTrue(
                testStory.storyInfo.comments > -2,
                "Story " + testStory.storyInfo.title + " parent story info comment count is invalid."
        );
        Assertions.assertTrue(
                testStory.storyInfo.kudos > -2,
                "Story " + testStory.storyInfo.title + " parent story info kudos count is invalid."
        );
        Assertions.assertTrue(
                testStory.storyInfo.bookmarks > -2,
                "Story " + testStory.storyInfo.title + " parent story info bookmark count is invalid."
        );
        Assertions.assertTrue(
                testStory.storyInfo.hits > -2,
                "Story " + testStory.storyInfo.title + " parent story info hit count is invalid."
        );

        // Assert current chapters are less than story chapters (Unless there was a ?)
        if (testStory.storyInfo.totalChapters > -1) { // gt comparison to confirm only counting numbers are used
            Assertions.assertTrue(
                    testStory.storyInfo.currentChapters <= testStory.storyInfo.totalChapters,
                    "Story " + testStory.storyInfo.title +
                            " parent story info current chapter count more than total chapter count."
            );
        }

        // Start asserting that object hashes are different from each other
        Assertions.assertNotEquals(
                testStory.creationHash, testStory.storyInfo.creationHash,
                "Story " + testStory.storyInfo.title + " hash matches its story info hash."
        );

        ArrayList<String> testHashList = new ArrayList<>();
        testHashList.add(testStory.creationHash);
        testHashList.add(testStory.storyInfo.creationHash);

        // Run chapter specific tests in a loop
        for (Chapter testChapter : testStory.chapters) {
            // Call runChapterTests
            runChapterTests(testChapter, testStory.creationTimestamp.toString());

            // Assert that there is no duplicate hashes
            Assertions.assertFalse(
                    testHashList.contains(testChapter.creationHash),
                    "Chapter " + testChapter.chapterTitle + " hash matches other hashes."
            );
            testHashList.add(testChapter.creationHash);

            if (testStory.chapters.size() > 1) {
                // Assert that story title and chapter titles are distinct if multi
                Assertions.assertNotEquals(
                        testStory.storyInfo.title, testChapter.chapterTitle,
                        "Chapter " + testChapter.chapterTitle + " title matches story title."
                );
            }
            else {
                // Assert that story title and chapter titles are the same if single
                Assertions.assertTrue(
                        testChapter.chapterTitle.contains(testStory.storyInfo.title),
                        "Chapter " + testChapter.chapterTitle +
                                " (single chapter) title does not contain story title."
                );
            }
        }
    }

    @Test
    public void testStoryParse() throws InterruptedException, ChapterContentNotFoundError, IngestorCanceledError,
            IngestorElementNotFoundError {
        JSONObject storyTestLinks = testLinks.getJSONObject("Story");
        Iterator<String> testLinksKeys = storyTestLinks.keys();
        while (testLinksKeys.hasNext()) {
            // Parse the story
            String testStoryName = testLinksKeys.next();
            String testStoryLink = storyTestLinks.getString(testStoryName);
            testDriver.get(testStoryLink);
            Story testStory = testIngestor.createStory(testDriver);

            // Test the story
            runStoryTests(testStory);
        }
    }

    @AfterAll
    public static void quitDriver() throws InterruptedException {
        System.out.println("Closing remote Selenium test driver...");
        testDriver.quit();

        System.out.println("Deleting Selenium container...");
        try {
            Process stopExec = testRuntime.exec(stopSeleniumContainerCmd);
            stopExec.waitFor();
            Process removeExec = testRuntime.exec(removeSeleniumContainerCmd);
            removeExec.waitFor();
        }
        catch (IOException e) {
            Assertions.fail(e.toString());
        }
    }
}
