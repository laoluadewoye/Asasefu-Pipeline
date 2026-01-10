package com.laoluade.ingestor.ao3.core;

// Core Error Package
import com.laoluade.ingestor.ao3.exceptions.*;

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

/**
 * <p>This test class is used to run unit tests and integration tests for the {@link ArchiveIngestor} class/service.</p>
 */
public class ArchiveIngestorTest {
    // Test objects
    /**
     * <p>This test attribute is used to read in test story links to utilize.</p>
     */
    private static JSONObject testLinks;

    /**
     * <p>This test attribute is used to set up the Selenium test driver.</p>
     */
    private static RemoteWebDriver testDriver;
    /**
     * <p>This test attribute is used to set up the test archive ingestor.</p>
     */
    private static ArchiveIngestor testIngestor;

    // Test values to check
    /**
     * <p>This test attribute is used to hold a list of possible AO3 story ratings.</p>
     */
    private static final ArrayList<String> expectedRatings = new ArrayList<>(Arrays.asList(
            "General Audiences", "Teen And Up Audiences", "Mature", "Explicit", "Not Rated"
    ));

    /**
     * <p>This test attribute is used to hold a list of possible AO3 story warnings.</p>
     */
    private static final ArrayList<String> expectedWarnings = new ArrayList<>(Arrays.asList(
            "Underage Sex", "Rape/Non-Con", "Graphic Depictions Of Violence", "Major Character Death",
            "Creator Chose Not To Use Archive Warnings", "No Archive Warnings Apply"
    ));

    /**
     * <p>This test attribute is used to hold a list of possible AO3 story categories.</p>
     */
    private static final ArrayList<String> expectedCategories = new ArrayList<>(Arrays.asList(
            "F/F", "F/M", "Gen", "M/M", "Multi", "Other"
    ));

    /**
     * <p>This test attribute is used to hold a list of possible AO3 story statuses.</p>
     */
    private static final ArrayList<String> expectedStatuses = new ArrayList<>(Arrays.asList(
            "Completed", "Updated", ArchiveIngestor.PLACEHOLDER
    ));

    // Test runtime stuff
    /**
     * <p>This test attribute is used to hold the runtime used to run system commands.</p>
     */
    private static final Runtime testRuntime = Runtime.getRuntime();

    /**
     * <p>This test attribute is used to hold the command for creating a new selenium container.</p>
     */
    private static final String[] runSeleniumContainerCmd = {
            "docker", "run", "-d", "-p", "4444:4444", "-p", "7900:7900", "--shm-size=\"2g\"",
            "--name", "archive-ingestor-test-container", "selenium/standalone-chrome:136.0-20251101"
    };

    /**
     * <p>This test attribute is used to hold the command for stopping a selenium container.</p>
     */
    private static final String[] stopSeleniumContainerCmd = {
            "docker", "stop", "archive-ingestor-test-container"
    };

    /**
     * <p>This test attribute is used to hold the command for starting a selenium container.</p>
     */
    private static final String[] startSeleniumContainerCmd = {
            "docker", "start", "archive-ingestor-test-container"
    };

    /**
     * <p>This test attribute is used to hold the command for removing a selenium container.</p>
     */
    private static final String[] removeSeleniumContainerCmd = {
            "docker", "rm", "archive-ingestor-test-container"
    };

    /**
     * <p>This test method is used to set up the test link, the archive ingestor, and the selenium container.</p>
     * @throws IOException If the <code>ArchiveIngestor.getJSONFromFilepath()</code> line experiences an I/O exception.
     * @throws InterruptedException If the <code>Thread.sleep()</code> is interrupted mid-execution.
     */
    @BeforeAll
    public static void setupTests() throws IOException, InterruptedException {
        System.out.println("Obtaining test links...");
        URL testLinksLocator = ArchiveIngestorTest.class.getResource("test_story_links.json");
        assert testLinksLocator != null;
        String testLinksPathDecoded = URLDecoder.decode(testLinksLocator.getPath(), StandardCharsets.UTF_8);
        testLinks = ArchiveIngestor.getJSONFromFilepath(testLinksPathDecoded);

        System.out.println("Creating archive ingestor instance...");
        testIngestor = new ArchiveIngestor(null, null, null, null, 3, 5, 10, 3, 3, 3);

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

    /**
     * <p>This test method is used to ensure the web driver is set up correctly.</p>
     */
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

    /**
     * <p>This test method is used to ensure that a bad AO3 work link is properly detected.</p>
     */
    @Test
    public void testBadWork() {
        try {
            testDriver.get("https://archiveofourown.org/works/XXXXXXXX");
            testIngestor.createChapter(testDriver, "");
        }
        catch (ArchiveVersionIncompatibleException | ArchiveParagraphsNotFoundException | ArchiveIngestorCanceledException |
               ArchiveElementNotFoundException | ArchivePageNotFoundException e) {
            System.out.println(e.toString());
        }
        catch (InterruptedException e) {
            Assertions.fail("For some reason the thread threw this exception.");
        }
        catch (Exception e) {
            Assertions.fail("Intentional bad link broke the ingestor in an unintentional way.");
        }
    }

    /**
     * <p>This test method is used to ensure the current OTW archive version of AO3 is supported.</p>
     */
    @Test
    public void testArchiveVersion() {
        testDriver.get("https://archiveofourown.org/works/XXXXXXXX");
        try {
            testIngestor.checkArchiveVersion(testDriver, "");
        } catch (ArchiveVersionIncompatibleException e) {
            System.out.println(e);
            Assertions.fail("Archive version is out of date. This is fine if it happens post-release but not here.");
        }
    }

    /**
     * <p>This test method is used to run a batch of chapter-level assertion tests.</p>
     * @param testArchiveChapter The archive chapter of interest.
     * @param storyCreationTimestamp The creation timestamp of a corresponding {@link ArchiveStory} object.
     */
    public void runChapterTests(ArchiveChapter testArchiveChapter, String storyCreationTimestamp) {
        // Assert chapter timestamps are good
        Assertions.assertInstanceOf(
                ZonedDateTime.class, testArchiveChapter.creationTimestamp,
                "Chapter " + testArchiveChapter.chapterTitle + " timestamp is not a ZonedDateTime object."
        );
        Assertions.assertEquals(
                "UTC", testArchiveChapter.creationTimestamp.getZone().toString(),
                "Chapter " + testArchiveChapter.chapterTitle + " timestamp is not set to UTC."
        );
        Assertions.assertNotEquals(
                testArchiveChapter.parentArchiveStoryInfo.creationTimestamp.toString(), testArchiveChapter.creationTimestamp.toString(),
                "Chapter " + testArchiveChapter.chapterTitle + " timestamp is the same as the parent story info timestamp."
        );
        Assertions.assertNotEquals(
                storyCreationTimestamp, testArchiveChapter.creationTimestamp.toString(),
                "Chapter " + testArchiveChapter.chapterTitle + " timestamp is the same as the parent story timestamp."
        );

        // Assert that the pageLink supplied is valid
        try {
            URI testURI = new URI(testArchiveChapter.pageLink);
            URL testURL = testURI.toURL();
        }
        catch (MalformedURLException | URISyntaxException e) {
            Assertions.fail("Chapter " + testArchiveChapter.chapterTitle + " page link is bad.");
        }

        // Assert that the paragraph list is not empty
        Assertions.assertNotEquals(
                0, testArchiveChapter.paragraphs.size(),
                "Chapter " + testArchiveChapter.chapterTitle + " does not contain the actual chapter."
        );

        // Assert JSONs can return properly
        JSONObject testJSONFatherful = testArchiveChapter.getJSONRepWithParent();
        JSONObject testJSONFatherless = testArchiveChapter.getJSONRepWithoutParent();

        Assertions.assertInstanceOf(
                JSONObject.class, testJSONFatherful,
                "Chapter " + testArchiveChapter.chapterTitle +
                        " JSON representation with parent is not a JSONObject object."
        );
        Assertions.assertInstanceOf(
                JSONObject.class, testJSONFatherless,
                "Chapter " + testArchiveChapter.chapterTitle +
                        " JSON representation without parent is not a JSONObject object."
        );

        // Assert JSON contains all values
        try {
            for (Field field : ArchiveChapter.class.getDeclaredFields()) {
                if (field.getName().equals("parentArchiveStoryInfo")) {
                    JSONObject psi = testJSONFatherful.getJSONObject(field.getName());
                    for (Field innerField : ArchiveStoryInfo.class.getDeclaredFields()) {
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

    /**
     * <p>This test method uses the chapter test links to test the functionality of the archive ingestor.</p>
     * @throws InterruptedException If the <code>testIngestor.createChapter()</code> line returns the exception.
     * @throws ArchiveParagraphsNotFoundException If the <code>testIngestor.createChapter()</code> line returns the exception.
     * @throws ArchiveIngestorCanceledException If the <code>testIngestor.createChapter()</code> line returns the exception.
     * @throws ArchiveElementNotFoundException If the <code>testIngestor.createChapter()</code> line returns the exception.
     * @throws ArchivePageNotFoundException If the <code>testIngestor.createChapter()</code> line returns the exception.
     */
    @Test
    public void testChapterParse() throws InterruptedException, ArchiveParagraphsNotFoundException, ArchiveIngestorCanceledException,
            ArchiveElementNotFoundException, ArchivePageNotFoundException {
        JSONObject chapterTestLinks = testLinks.getJSONObject("Chapter");
        Iterator<String> testLinksKeys = chapterTestLinks.keys();
        while (testLinksKeys.hasNext()) {
            // Parse the chapter
            String testStoryName = testLinksKeys.next();
            String testStoryLink = chapterTestLinks.getString(testStoryName);
            testDriver.get(testStoryLink);
            ArchiveChapter testArchiveChapter = testIngestor.createChapter(testDriver, "");

            // Test the chapter
            runChapterTests(testArchiveChapter, testArchiveChapter.parentArchiveStoryInfo.creationTimestamp.toString());
        }
    }

    /**
     * <p>This test method is used to run a batch of story-level assertion tests.</p>
     * @param testArchiveStory The archive story of interest.
     */
    public void runStoryTests(ArchiveStory testArchiveStory) {
        // Assert story timestamps are good
        Assertions.assertInstanceOf(
                ZonedDateTime.class, testArchiveStory.creationTimestamp,
                "Story timestamp is not a ZonedDateTime object."
        );
        Assertions.assertEquals(
                "UTC", testArchiveStory.creationTimestamp.getZone().toString(),
                "Story timestamp is not set to UTC."
        );
        Assertions.assertInstanceOf(
                ZonedDateTime.class, testArchiveStory.archiveStoryInfo.creationTimestamp,
                "Story info timestamp is not a ZonedDateTime object."
        );
        Assertions.assertEquals(
                "UTC", testArchiveStory.archiveStoryInfo.creationTimestamp.getZone().toString(),
                "Story info timestamp is not set to UTC."
        );
        Assertions.assertNotEquals(
                testArchiveStory.creationTimestamp.toString(), testArchiveStory.archiveStoryInfo.toString(),
                "Story " + testArchiveStory.archiveStoryInfo.title + " timestamp is the same as the story info timestamp."
        );

        // Assert that the author list is not empty
        Assertions.assertNotEquals(
                0, testArchiveStory.archiveStoryInfo.authors.size(),
                "Story " + testArchiveStory.archiveStoryInfo.title + " does not contain the author(s)."
        );

        // Assert that the kudos math checks out
        Integer totalKudos = testArchiveStory.archiveStoryInfo.registeredKudos.size() +
                testArchiveStory.archiveStoryInfo.unregisteredKudos;
        Assertions.assertEquals(
                testArchiveStory.archiveStoryInfo.kudos, totalKudos,
                "Story " + testArchiveStory.archiveStoryInfo.title + " kudos calculations are off."
        );

        // Assert that the status doesn't have a colon
        Assertions.assertFalse(
                testArchiveStory.archiveStoryInfo.status.contains(":"),
                "Story " + testArchiveStory.archiveStoryInfo.title + " status contains a colon."
        );

        // Assert the rating is right
        Assertions.assertEquals(
                1, testArchiveStory.archiveStoryInfo.ratings.size(),
                "Story " + testArchiveStory.archiveStoryInfo.title + " contains more than one rating."
        );
        Assertions.assertTrue(expectedRatings.contains(
                        testArchiveStory.archiveStoryInfo.ratings.getFirst()),
                "Story " + testArchiveStory.archiveStoryInfo.title + " rating is invalid."
        );

        // Assert the warning items is good
        for (String warningItem : testArchiveStory.archiveStoryInfo.warnings) {
            Assertions.assertTrue(
                    expectedWarnings.contains(warningItem),
                    "Story " + testArchiveStory.archiveStoryInfo.title + " warning " + warningItem + " is invalid."
            );
        }

        // Assert the category items are good
        for (String categoryItem : testArchiveStory.archiveStoryInfo.categories) {
            Assertions.assertTrue(
                    expectedCategories.contains(categoryItem),
                    "Story " + testArchiveStory.archiveStoryInfo.title + " category " + categoryItem + " is invalid."
            );
        }

        // Assert time statistics are good
        Assertions.assertInstanceOf(
                LocalDate.class, testArchiveStory.archiveStoryInfo.published,
                "Story " + testArchiveStory.archiveStoryInfo.title +
                        " parent story info publish date is not a LocalDate Object."
        );
        Assertions.assertInstanceOf(
                LocalDate.class, testArchiveStory.archiveStoryInfo.statusWhen,
                "Story " + testArchiveStory.archiveStoryInfo.title +
                        " parent story info status date is not a LocalDate Object."
        );

        boolean publishBefore = testArchiveStory.archiveStoryInfo.published.isBefore(testArchiveStory.archiveStoryInfo.statusWhen);
        boolean publishEqual = testArchiveStory.archiveStoryInfo.published.isEqual(testArchiveStory.archiveStoryInfo.statusWhen);
        Assertions.assertTrue(
                publishBefore | publishEqual,
                "Story " + testArchiveStory.archiveStoryInfo.title +
                        " parent story info publish date is after the updated/completed date."
        );

        // Assert the status is good
        Assertions.assertTrue(
                expectedStatuses.contains(testArchiveStory.archiveStoryInfo.status),
                "Story " + testArchiveStory.archiveStoryInfo.title + " parent story info status is invalid."
        );

        // Assert series items are good
        for (String series : testArchiveStory.archiveStoryInfo.series) {
            Assertions.assertFalse(
                    series.contains("Part"),
                    "Story " + testArchiveStory.archiveStoryInfo.title + " parent story series " + series + " is invalid."
            );
        }

        // Assert numbers are good
        Assertions.assertTrue(
                testArchiveStory.archiveStoryInfo.words > -2,
                "Story " + testArchiveStory.archiveStoryInfo.title + " parent story info word count is invalid."
        );
        Assertions.assertTrue(
                testArchiveStory.archiveStoryInfo.currentChapters > -2,
                "Story " + testArchiveStory.archiveStoryInfo.title + " parent story info current chapter count is invalid."
        );
        Assertions.assertTrue(
                testArchiveStory.archiveStoryInfo.totalChapters > -2,
                "Story " + testArchiveStory.archiveStoryInfo.title + " parent story info total chapter count is invalid."
        );
        Assertions.assertTrue(
                testArchiveStory.archiveStoryInfo.comments > -2,
                "Story " + testArchiveStory.archiveStoryInfo.title + " parent story info comment count is invalid."
        );
        Assertions.assertTrue(
                testArchiveStory.archiveStoryInfo.kudos > -2,
                "Story " + testArchiveStory.archiveStoryInfo.title + " parent story info kudos count is invalid."
        );
        Assertions.assertTrue(
                testArchiveStory.archiveStoryInfo.bookmarks > -2,
                "Story " + testArchiveStory.archiveStoryInfo.title + " parent story info bookmark count is invalid."
        );
        Assertions.assertTrue(
                testArchiveStory.archiveStoryInfo.hits > -2,
                "Story " + testArchiveStory.archiveStoryInfo.title + " parent story info hit count is invalid."
        );

        // Assert current chapters are less than story chapters (Unless there was a ?)
        if (testArchiveStory.archiveStoryInfo.totalChapters > -1) { // gt comparison to confirm only counting numbers are used
            Assertions.assertTrue(
                    testArchiveStory.archiveStoryInfo.currentChapters <= testArchiveStory.archiveStoryInfo.totalChapters,
                    "Story " + testArchiveStory.archiveStoryInfo.title +
                            " parent story info current chapter count more than total chapter count."
            );
        }

        // Start asserting that object hashes are different from each other
        Assertions.assertNotEquals(
                testArchiveStory.creationHash, testArchiveStory.archiveStoryInfo.creationHash,
                "Story " + testArchiveStory.archiveStoryInfo.title + " hash matches its story info hash."
        );

        ArrayList<String> testHashList = new ArrayList<>();
        testHashList.add(testArchiveStory.creationHash);
        testHashList.add(testArchiveStory.archiveStoryInfo.creationHash);

        // Run chapter specific tests in a loop
        for (ArchiveChapter testArchiveChapter : testArchiveStory.archiveChapters) {
            // Call runChapterTests
            runChapterTests(testArchiveChapter, testArchiveStory.creationTimestamp.toString());

            // Assert that there is no duplicate hashes
            Assertions.assertFalse(
                    testHashList.contains(testArchiveChapter.creationHash),
                    "Chapter " + testArchiveChapter.chapterTitle + " hash matches other hashes."
            );
            testHashList.add(testArchiveChapter.creationHash);

            if (testArchiveStory.archiveChapters.size() > 1) {
                // Assert that story title and chapter titles are distinct if multi
                Assertions.assertNotEquals(
                        testArchiveStory.archiveStoryInfo.title, testArchiveChapter.chapterTitle,
                        "Chapter " + testArchiveChapter.chapterTitle + " title matches story title."
                );
            }
            else {
                // Assert that story title and chapter titles are the same if single
                Assertions.assertTrue(
                        testArchiveChapter.chapterTitle.contains(testArchiveStory.archiveStoryInfo.title),
                        "Chapter " + testArchiveChapter.chapterTitle +
                                " (single chapter) title does not contain story title."
                );
            }
        }
    }

    /**
     * <p>This test method uses the story test links to test the functionality of the archive ingestor.</p>
     * @throws InterruptedException If the <code>testIngestor.createStory()</code> line returns the exception.
     * @throws ArchiveParagraphsNotFoundException If the <code>testIngestor.createStory()</code> line returns the exception.
     * @throws ArchiveIngestorCanceledException If the <code>testIngestor.createStory()</code> line returns the exception.
     * @throws ArchiveElementNotFoundException If the <code>testIngestor.createStory()</code> line returns the exception.
     * @throws ArchivePageNotFoundException If the <code>testIngestor.createStory()</code> line returns the exception.
     */
    @Test
    public void testStoryParse() throws InterruptedException, ArchiveParagraphsNotFoundException, ArchiveIngestorCanceledException,
            ArchiveElementNotFoundException, ArchivePageNotFoundException {
        JSONObject storyTestLinks = testLinks.getJSONObject("Story");
        Iterator<String> testLinksKeys = storyTestLinks.keys();
        while (testLinksKeys.hasNext()) {
            // Parse the story
            String testStoryName = testLinksKeys.next();
            String testStoryLink = storyTestLinks.getString(testStoryName);
            testDriver.get(testStoryLink);
            ArchiveStory testArchiveStory = testIngestor.createStory(testDriver, "");

            // Test the story
            runStoryTests(testArchiveStory);
        }
    }

    /**
     * <p>This test method is used to shut down running aspects of the archive ingestor tests.</p>
     * @throws InterruptedException If the <code>waitFor()</code> lines is interrupted mid-execution.
     */
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
