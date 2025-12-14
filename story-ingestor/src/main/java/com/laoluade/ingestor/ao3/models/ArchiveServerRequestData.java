package com.laoluade.ingestor.ao3.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

@Getter
@ToString
@NoArgsConstructor
public class ArchiveServerRequestData {
    private String pageLink;
    private String sessionNickname;
    @Setter private boolean nicknameSent;

    public ArchiveServerRequestData(String pageLink) {
        createURLTest(pageLink);
        this.sessionNickname = "";
        this.nicknameSent = false;
    }

    public ArchiveServerRequestData(String pageLink, String sessionNickname) {
        createURLTest(pageLink);
        createNicknameTest(sessionNickname);

        if (!sessionNickname.isBlank()) { // If whitespace was sent, we can ignore and just reset it later
            this.nicknameSent = true;
        }
    }

    public void setPageLink(String pageLink) {
        createURLTest(pageLink);
    }

    public void setSessionNickname(String sessionNickname) {
        createNicknameTest(sessionNickname);
    }

    public void createURLTest(String pageLink) {
        try {
            new URI(pageLink).toURL();

            final Pattern storyLinkPattern = Pattern.compile(
                    "^https://archiveofourown\\.org/works/[0-9]+$", Pattern.CASE_INSENSITIVE
            );
            final Pattern chapterLinkPattern = Pattern.compile(
                    "^https://archiveofourown\\.org/works/[0-9]+/chapters/[0-9]+$", Pattern.CASE_INSENSITIVE
            );

            boolean isStoryLink = storyLinkPattern.matcher(pageLink).matches();
            boolean isChapterLink = chapterLinkPattern.matcher(pageLink).matches();

            if (isStoryLink || isChapterLink) {
                this.pageLink = pageLink;
            }
            else {
                this.pageLink = "";
            }
        }
        catch (IllegalArgumentException | URISyntaxException | MalformedURLException e) {
            this.pageLink = "";
        }
    }

    public void createNicknameTest(String sessionNickname) {
        Pattern nicknamePattern = Pattern.compile("^[a-zA-Z0-9_-]*$", Pattern.CASE_INSENSITIVE);
        if (nicknamePattern.matcher(sessionNickname).matches()) {
            this.sessionNickname = sessionNickname;
        }
        else {
            this.sessionNickname = "";
        }
    }
}
