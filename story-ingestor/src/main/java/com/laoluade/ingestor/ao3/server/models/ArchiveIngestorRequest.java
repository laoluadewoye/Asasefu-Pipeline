package com.laoluade.ingestor.ao3.server.models;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ArchiveIngestorRequest {
    private URL pagelinkURL;
    private String sessionNickname;

    public ArchiveIngestorRequest(String pageLink) throws URISyntaxException, MalformedURLException {
        URI pageLinkURI = new URI(pageLink);
        this.pagelinkURL = pageLinkURI.toURL();
    }

    public ArchiveIngestorRequest(String pageLink, String sessionNickname) throws URISyntaxException,
            MalformedURLException {
        URI pageLinkURI = new URI(pageLink);
        this.pagelinkURL = pageLinkURI.toURL();
        this.sessionNickname = sessionNickname;
    }

    public void setPageLinkURL(String pageLink) throws URISyntaxException, MalformedURLException {
        URI pageLinkURI = new URI(pageLink);
        this.pagelinkURL = pageLinkURI.toURL();
    }

    public URL getPageLinkURL() { return this.pagelinkURL; }

    public void setSessionNickname(String sessionNickname) {
        this.sessionNickname = sessionNickname;
    }

    public String getSessionNickname() { return this.sessionNickname; }
}
