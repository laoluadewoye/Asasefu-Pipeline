package com.laoluade.ingestor.ao3;

import com.laoluade.ingestor.ao3.core.ArchiveChapter;
import com.laoluade.ingestor.ao3.core.ArchiveIngestor;
import com.laoluade.ingestor.ao3.core.ArchiveStory;
import com.laoluade.ingestor.ao3.exceptions.ArchiveElementNotFoundException;
import com.laoluade.ingestor.ao3.exceptions.ArchiveIngestorCanceledException;
import com.laoluade.ingestor.ao3.exceptions.ArchivePageNotFoundException;
import com.laoluade.ingestor.ao3.exceptions.ArchiveParagraphsNotFoundException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ArchiveCLI {
    public static void main(String[] args) throws IOException, URISyntaxException, ArchivePageNotFoundException,
            ArchiveIngestorCanceledException, ArchiveParagraphsNotFoundException, InterruptedException,
            ArchiveElementNotFoundException {
        ArchiveIngestor ai = new ArchiveIngestor(null, null, null, null, 3, 5, 10, 3, 3, 3);
        URL cl = new URI("http://localhost:4444").toURL();
        RemoteWebDriver d = new RemoteWebDriver(cl, new ChromeOptions());
        d.get("https://archiveofourown.org/works/73057466"); // TODO: Fix I think I am problem
        try {
            ArchiveStory c = ai.createStory(d, "");
        } catch (Exception e) {
            System.out.println(e);
        }
        finally {
            d.quit();
        }
    }
}
