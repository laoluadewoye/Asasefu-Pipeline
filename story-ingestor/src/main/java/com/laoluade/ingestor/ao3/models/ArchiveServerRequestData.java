package com.laoluade.ingestor.ao3.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

@Getter
@ToString
@NoArgsConstructor
public class ArchiveServerRequestData {
    private String pageLink;
    @Setter private String sessionNickname;

    public ArchiveServerRequestData(String pageLink) {
        createURLTest(pageLink);
        this.sessionNickname = "";
    }

    public ArchiveServerRequestData(String pageLink, String sessionNickname) {
        createURLTest(pageLink);
        this.sessionNickname = sessionNickname;
    }

    public void setPageLink(String pageLink) {
        createURLTest(pageLink);
    }

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
