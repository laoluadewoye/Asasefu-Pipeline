package com.laoluade.ingestor.ao3.models;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class ArchiveIngestorRequest {
    private String pageLink;
    private String sessionNickname;

    public ArchiveIngestorRequest() {}

    public ArchiveIngestorRequest(String pageLink) {
        createURLTest(pageLink);
        this.sessionNickname = "";
    }

    public ArchiveIngestorRequest(String pageLink, String sessionNickname) {
        createURLTest(pageLink);
        this.sessionNickname = sessionNickname;
    }

    public void setPageLink(String pageLink) {
        createURLTest(pageLink);
    }

    public String getPageLink() { return this.pageLink; }

    public void setSessionNickname(String sessionNickname) {
        this.sessionNickname = sessionNickname;
    }

    public String getSessionNickname() { return this.sessionNickname; }

    public void createURLTest(String pageLink) {
        try {
            new URI(pageLink).toURL();
            this.pageLink = pageLink;
        }
        catch (URISyntaxException | MalformedURLException e) {
            this.pageLink = "";
        }
    }
}
