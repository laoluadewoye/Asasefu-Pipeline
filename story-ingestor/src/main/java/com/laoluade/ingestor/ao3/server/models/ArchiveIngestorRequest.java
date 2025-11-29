package com.laoluade.ingestor.ao3.server.models;

import com.laoluade.ingestor.ao3.core.StoryInfo;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ArchiveIngestorRequest {
    private URL pagelinkURL;
    private StoryInfo storyInfo;

    public ArchiveIngestorRequest(String pageLink) throws URISyntaxException, MalformedURLException {
        URI pageLinkURI = new URI(pageLink);
        this.pagelinkURL = pageLinkURI.toURL();
    }

    public ArchiveIngestorRequest(String pageLink, String storyInfoJSONString) throws URISyntaxException,
            MalformedURLException {
        URI pageLinkURI = new URI(pageLink);
        this.pagelinkURL = pageLinkURI.toURL();
        this.storyInfo = StoryInfo.fromJSONString(storyInfoJSONString);
    }

    public void setPageLinkURL(String pageLink) throws URISyntaxException, MalformedURLException {
        URI pageLinkURI = new URI(pageLink);
        this.pagelinkURL = pageLinkURI.toURL();
    }

    public URL getPageLinkURL() { return this.pagelinkURL; }

    public void setStoryInfo(String storyInfoJSONString) {
        this.storyInfo = StoryInfo.fromJSONString(storyInfoJSONString);
    }

    public StoryInfo getStoryInfo() { return this.storyInfo; }
}
