package com.laoluade.ingestor.ao3;

import com.laoluade.ingestor.ao3.core.ArchiveChapter;
import com.laoluade.ingestor.ao3.core.ArchiveIngestor;
import com.laoluade.ingestor.ao3.core.ArchiveStory;
import com.laoluade.ingestor.ao3.exceptions.ArchiveElementNotFoundException;
import com.laoluade.ingestor.ao3.exceptions.ArchiveIngestorCanceledException;
import com.laoluade.ingestor.ao3.exceptions.ArchivePageNotFoundException;
import com.laoluade.ingestor.ao3.exceptions.ArchiveParagraphsNotFoundException;
import org.json.JSONObject;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

public class ArchiveCLI {
    public static void main(String[] args) throws IOException, URISyntaxException, ArchivePageNotFoundException,
            ArchiveIngestorCanceledException, ArchiveParagraphsNotFoundException, InterruptedException,
            ArchiveElementNotFoundException {
        ArchiveIngestor ai = new ArchiveIngestor(null, null, null, null, 3, 5, 10, 3, 3, 3);
        URL cl = new URI("http://localhost:4444").toURL();
        RemoteWebDriver d = new RemoteWebDriver(cl, new ChromeOptions());
        d.get("https://archiveofourown.org/works/57414697/chapters/172512946"); // TODO: Fix I think I am problem
        try {
            ArchiveChapter a = ai.createChapter(d, "");
            JSONObject aj = a.getJSONRepWithParent();
            System.out.println(aj.toString(4));
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        finally {
            d.quit();
        }
    }
}
