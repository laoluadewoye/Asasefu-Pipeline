package com.laoluade.pipeline;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Chapter {
    public ZonedDateTime timestamp;
    public Story parentStory;
    public String pageTitle;
    public Integer chapterNumber;

    public Chapter(Story parentStory, String pageTitle, String chapterNumber) {
        // Get current timestamp
        this.timestamp = ZonedDateTime.now(ZoneId.of("UTC"));

        // Set parent story
        this.parentStory = parentStory;

        // Set chapter information
        this.pageTitle = pageTitle;
        this.chapterNumber = Integer.parseInt(chapterNumber);
    }
}
