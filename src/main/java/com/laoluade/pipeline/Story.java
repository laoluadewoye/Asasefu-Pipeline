package com.laoluade.pipeline;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.time.LocalDate;

public class Story {
    public ZonedDateTime timestamp;
    public String series;
    public ArrayList<String> ratingItems;
    public ArrayList<String> warningItems;
    public ArrayList<String> categoryItems;
    public ArrayList<String> fandomItems;
    public ArrayList<String> relationshipItems;
    public ArrayList<String> characterItems;
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
                 ArrayList<String> warningItems, ArrayList<String> categoryItems, ArrayList<String> fandomItems,
                 ArrayList<String> relationshipItems, ArrayList<String> characterItems, ArrayList<String> freeformItems,
                 String language, String published, String status, String statusWhen, String words, String chapters,
                 String comments, String kudos, String bookmarks, String hits) {
        // Get current timestamp
        this.timestamp = ZonedDateTime.now(ZoneId.of("UTC"));

        // Set core information
        this.series = series;
        this.ratingItems = ratingItems;
        this.warningItems = warningItems;
        this.categoryItems = categoryItems;
        this.fandomItems = fandomItems;
        this.relationshipItems = relationshipItems;
        this.characterItems = characterItems;
        this.freeformItems = freeformItems;
        this.language = language;

        // Setup statistics
        this.published = LocalDate.parse(published);
        this.words = parseInitString(words);

        this.status = status.replace(":", "");
        if (!status.equals(ArchiveIngestor.PLACEHOLDER)) {
            this.statusWhen = LocalDate.parse(statusWhen);
        }
        else {
            this.statusWhen = LocalDate.now();
        }

        String[] chaptersSplit = chapters.split("/");
        this.currentChapters = parseInitString(chaptersSplit[0]);
        this.totalChapters = parseInitString(chaptersSplit[1]);

        this.comments = parseInitString(comments);
        this.kudos = parseInitString(kudos);
        this.bookmarks = parseInitString(bookmarks);
        this.hits = parseInitString(hits);
    }

    private Integer parseInitString(String initString) {
        if (initString.equals("?")) {
            return -1;
        }
        else if (!initString.equals(ArchiveIngestor.PLACEHOLDER)) {
            return Integer.parseInt(initString.replace(",", ""));
        }
        else {
            return 0;
        }
    }
}
