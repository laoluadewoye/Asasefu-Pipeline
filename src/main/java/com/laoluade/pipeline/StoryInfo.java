package com.laoluade.pipeline;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.time.LocalDate;

public class StoryInfo {
    // Meta items
    public ZonedDateTime timestamp;
    public ArrayList<String> ratings;
    public ArrayList<String> warnings;
    public ArrayList<String> categories;
    public ArrayList<String> fandoms;
    public ArrayList<String> relationships;
    public ArrayList<String> characters;
    public ArrayList<String> additionalTags;
    public String language;
    public ArrayList<String> series;
    public ArrayList<String> collections;
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

    // Preface items
    public boolean isSet = false;
    public String title;
    public ArrayList<String> authors;
    public ArrayList<String> summary;
    public ArrayList<String> associations;
    public ArrayList<String> startNotes;
    public ArrayList<String> endNotes;

    public StoryInfo(ArrayList<String> ratings,
                     ArrayList<String> warnings, ArrayList<String> categories, ArrayList<String> fandoms,
                     ArrayList<String> relationships, ArrayList<String> characters,
                     ArrayList<String> additionalTags, String language, ArrayList<String> series,
                     ArrayList<String> collections, String published, String status, String statusWhen, String words, String chapters,
                     String comments, String kudos, String bookmarks, String hits) {
        // Get current timestamp
        this.timestamp = ZonedDateTime.now(ZoneId.of("UTC"));

        // Set core information
        this.ratings = ratings;
        this.warnings = warnings;
        this.categories = categories;
        this.fandoms = fandoms;
        this.relationships = relationships;
        this.characters = characters;
        this.additionalTags = additionalTags;
        this.language = language;
        this.series = new ArrayList<>(series.stream().filter(str -> !str.contains("Part")).toList());
        this.collections = collections;

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

    public void setPrefaceInfo(String title, ArrayList<String> authors, ArrayList<String> summary,
                               ArrayList<String> associations, ArrayList<String> startNotes,
                               ArrayList<String> endNotes) {
        // Set preface values
        this.title = title;
        this.authors = authors;
        this.summary = summary;
        this.startNotes = startNotes;
        this.endNotes = endNotes;

        // Filter association list before setting
        this.associations = new ArrayList<>();
        for (String association : associations) {
            if (association.contains("For")) {
                this.associations.add(association);
            }
            else if (association.contains("translation")) {
                this.associations.add(association);
            }
        }

        // Set setting flag
        this.isSet = true;
    }
}
