package com.laoluade.ingestor.ao3.core;

import com.google.common.hash.Hashing;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class Chapter {
    // Meta items - self created
    public ZonedDateTime creationTimestamp;
    public String creationHash;

    // Meta items - passed in
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
        this.creationTimestamp = ZonedDateTime.now(ZoneId.of("UTC"));

        // Set parent story
        this.parentStoryInfo = parentStoryInfo;

        // Set chapter information
        this.pageTitle = pageTitle;
        this.chapterTitle = chapterTitle;
        this.summary = summary;
        this.startNotes = startNotes;
        this.endNotes = endNotes;
        this.paragraphs = paragraphs;

        // Generate the hash using chapter information except for paragraphs
        String creationHashInput = this.chapterTitle + String.join("", this.summary) +
                String.join("", this.startNotes) + String.join("", this.endNotes) +
                this.parentStoryInfo.creationHash;
        this.creationHash = Hashing.sha256().hashString(creationHashInput, StandardCharsets.UTF_8).toString();
    }

    public void setPageLink(String pageLink) {
        this.pageLink = pageLink;
    }

    public void setComments(JSONObject comments) {
        this.comments = comments;
    }

    public JSONObject getJSONRepWithParent() {
        JSONObject rep = new JSONObject();
        rep.put("creationTimestamp", this.creationTimestamp.toString());
        rep.put("creationHash", this.creationHash);
        rep.put("parentStoryInfo", this.parentStoryInfo.getJSONRep());
        rep.put("pageTitle", this.pageTitle);
        rep.put("pageLink", this.pageLink);
        rep.put("chapterTitle", this.chapterTitle);
        rep.put("summary", new JSONArray(this.summary));
        rep.put("startNotes", new JSONArray(this.startNotes));
        rep.put("endNotes", new JSONArray(this.endNotes));
        rep.put("paragraphs", new JSONArray(this.paragraphs));
        rep.put("comments", this.comments);
        return rep;
    }

    public JSONObject getJSONRepWithoutParent() {
        JSONObject rep = new JSONObject();
        rep.put("creationTimestamp", this.creationTimestamp.toString());
        rep.put("creationHash", this.creationHash);
        rep.put("pageTitle", this.pageTitle);
        rep.put("pageLink", this.pageLink);
        rep.put("chapterTitle", this.chapterTitle);
        rep.put("summary", new JSONArray(this.summary));
        rep.put("startNotes", new JSONArray(this.startNotes));
        rep.put("endNotes", new JSONArray(this.endNotes));
        rep.put("paragraphs", new JSONArray(this.paragraphs));
        rep.put("comments", this.comments);
        return rep;
    }
}
