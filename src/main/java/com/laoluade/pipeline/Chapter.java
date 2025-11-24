package com.laoluade.pipeline;

import org.json.JSONObject;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class Chapter {
    public ZonedDateTime timestamp;
    public StoryInfo parentStoryInfo;
    public String pageTitle;
    public String pageLink;
    public String chapterTitle;
    public ArrayList<String> summary;
    public ArrayList<String> startNotes;
    public ArrayList<String> endNotes;
    public ArrayList<String> paragraphs;
    public JSONObject comments;

    public Chapter(StoryInfo parentStoryInfo, String pageTitle, String chapterTitle, ArrayList<String> summary,
                   ArrayList<String> startNotes, ArrayList<String> endNotes, ArrayList<String> paragraphs) {
        // Get current timestamp
        this.timestamp = ZonedDateTime.now(ZoneId.of("UTC"));

        // Set parent story
        this.parentStoryInfo = parentStoryInfo;

        // Set chapter information
        this.pageTitle = pageTitle;
        this.chapterTitle = chapterTitle;
        this.summary = summary;
        this.startNotes = startNotes;
        this.endNotes = endNotes;
        this.paragraphs = paragraphs;
    }

    public void setPageLink(String pageLink) {
        this.pageLink = pageLink;
    }

    public void setComments(JSONObject comments) {
        this.comments = comments;
    }
}
