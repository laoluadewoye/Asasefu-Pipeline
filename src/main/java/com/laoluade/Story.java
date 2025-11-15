package com.laoluade;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.Objects;

public class Story {
    public ZonedDateTime timestamp;
    public String series;
    public ArrayList<String> ratingItems;
    public ArrayList<String> warningItems;
    public ArrayList<String> categoryItems;
    public ArrayList<String> fandomItems;
    public ArrayList<String> freeformItems;
    public String language;
    public LocalDate published;
    public String status;
    public LocalDate statusWhen;
    public Integer words;
    public Integer currentChapters;
    public Integer totalChapters;
    public Integer comments;
    public Integer kudos;
    public Integer bookmarks;
    public Integer hits;

    public Story(String series, ArrayList<String> ratingItems,
                 ArrayList<String> warningItems, ArrayList<String> categoryItems,
                 ArrayList<String> fandomItems, ArrayList<String> freeformItems, String language,
                 String published, String status, String statusWhen, String words,
                 String chapters, String comments, String kudos, String bookmarks,
                 String hits) {
        // Get current timestamp
        this.timestamp = ZonedDateTime.now(ZoneId.of("UTC"));

        // Set core information
        this.series = series;
        this.ratingItems = ratingItems;
        this.warningItems = warningItems;
        this.categoryItems = categoryItems;
        this.fandomItems = fandomItems;
        this.freeformItems = freeformItems;
        this.language = language;

        // Setup statistics
        this.published = LocalDate.parse(published);
        this.words = Integer.parseInt(words.replace(",", ""));

        this.status = status.replace(":", "");
        if (!status.equals(ArchiveIngestor.PLACEHOLDER)) {
            this.statusWhen = LocalDate.parse(statusWhen);
        }
        else {
            this.statusWhen = LocalDate.now();
        }

        String[] chaptersSplit = chapters.split("/");
        this.currentChapters = Integer.parseInt(chaptersSplit[0].replace(",", ""));
        this.totalChapters = Integer.parseInt(chaptersSplit[1].replace(",", ""));

        this.comments = Integer.parseInt(comments.replace(",", ""));
        this.kudos = Integer.parseInt(kudos.replace(",", ""));
        this.bookmarks = Integer.parseInt(bookmarks.replace(",", ""));
        this.hits = Integer.parseInt(hits.replace(",", ""));
    }
}
