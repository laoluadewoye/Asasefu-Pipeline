package com.laoluade.ingestor.ao3.core;

import com.google.common.hash.Hashing;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.util.ArrayList;

public class StoryInfo {
    // Meta items - self created
    public ZonedDateTime creationTimestamp;
    public String creationHash;

    // Meta items - passed in
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

    // Other information
    public ArrayList<String> registeredKudos;
    public Integer unnamedRegisteredKudos;
    public Integer guestKudos;
    public ArrayList<String> publicBookmarks;


    // Empty constructor for manual construction
    public StoryInfo() {}

    // Default constructor for parsing needs
    public StoryInfo(ArrayList<String> ratings,
                     ArrayList<String> warnings, ArrayList<String> categories, ArrayList<String> fandoms,
                     ArrayList<String> relationships, ArrayList<String> characters,
                     ArrayList<String> additionalTags, String language, ArrayList<String> series,
                     ArrayList<String> collections, String published, String status, String statusWhen, String words, String chapters,
                     String comments, String kudos, String bookmarks, String hits) {
        // Get current timestamp
        this.creationTimestamp = ZonedDateTime.now(ZoneId.of("UTC"));

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

    // TODO: Make sure front end can handle the negative 1
    public static Integer parseInitString(String initString) {
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

        // Generate the hash using preface information
        String creationHashInput = this.title + String.join("", this.authors) +
                String.join("", this.summary) + String.join("", this.associations) +
                String.join("", this.startNotes) + String.join("", this.endNotes);
        this.creationHash = Hashing.sha256().hashString(creationHashInput, StandardCharsets.UTF_8).toString();
    }

    public void setKudosList(ArrayList<String> kudosList) {
        // Check last value in case its saying there are more unspecified registered users
        try {
            this.unnamedRegisteredKudos = Integer.parseInt(kudosList.getLast().split(" ")[0]);
            kudosList.removeLast();
        }
        catch (NumberFormatException e) {
            this.unnamedRegisteredKudos = 0;
        }

        // Set values
        this.registeredKudos = kudosList;
        this.guestKudos = this.kudos - this.registeredKudos.size() - this.unnamedRegisteredKudos;
    }

    public void setPublicBookmarkList(ArrayList<String> bookmarkList) {
        this.publicBookmarks = bookmarkList;
    }

    public JSONObject getJSONRep() {
        JSONObject rep = new JSONObject();
        rep.put("creationTimestamp", this.creationTimestamp.toString());
        rep.put("creationHash", this.creationHash);
        rep.put("ratings", new JSONArray(this.ratings));
        rep.put("warnings", new JSONArray(this.warnings));
        rep.put("categories", new JSONArray(this.categories));
        rep.put("fandoms", new JSONArray(this.fandoms));
        rep.put("relationships", new JSONArray(this.relationships));
        rep.put("characters", new JSONArray(this.characters));
        rep.put("additionalTags", new JSONArray(this.additionalTags));
        rep.put("language", this.language);
        rep.put("series", new JSONArray(this.series));
        rep.put("collections", new JSONArray(this.collections));
        rep.put("published", this.published.toString());
        rep.put("status", this.status);
        rep.put("statusWhen", this.statusWhen.toString());
        rep.put("words", this.words);
        rep.put("currentChapters", this.currentChapters);
        rep.put("totalChapters", this.totalChapters);
        rep.put("comments", this.comments);
        rep.put("kudos", this.kudos);
        rep.put("bookmarks", this.bookmarks);
        rep.put("hits", this.hits);
        rep.put("title", this.title);
        rep.put("authors", new JSONArray(this.authors));
        rep.put("summary", new JSONArray(this.summary));
        rep.put("associations", new JSONArray(this.associations));
        rep.put("startNotes", new JSONArray(this.startNotes));
        rep.put("endNotes", new JSONArray(this.endNotes));
        rep.put("registeredKudos", new JSONArray(this.registeredKudos));
        rep.put("unnamedRegisteredKudos", this.unnamedRegisteredKudos);
        rep.put("guestKudos", this.guestKudos);
        rep.put("publicBookmarks", new JSONArray(this.publicBookmarks));
        return rep;
    }

    public static ArrayList<String> convertJSONArrayToArrayList(JSONArray oldJSONArray) {
        ArrayList<String> newArrayList = new ArrayList<>();
        for (int i = 0; i < oldJSONArray.length(); i++) {
            newArrayList.add(oldJSONArray.getString(i));
        }
        return newArrayList;
    }

    // TODO: Create a test for this
    public static StoryInfo fromJSONString(String jsonString) {
        JSONObject newStoryInfoJSON = new JSONObject(jsonString);
        StoryInfo newStoryInfo = new StoryInfo();

        newStoryInfo.creationTimestamp = ZonedDateTime.parse(newStoryInfoJSON.getString("creationTimestamp"));
        newStoryInfo.creationHash = newStoryInfoJSON.getString("creationHash");
        newStoryInfo.ratings = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("ratings"));
        newStoryInfo.warnings = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("warnings"));
        newStoryInfo.categories = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("categories"));
        newStoryInfo.fandoms = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("fandoms"));
        newStoryInfo.relationships = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("relationships"));
        newStoryInfo.characters = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("characters"));
        newStoryInfo.additionalTags = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("additionalTags"));
        newStoryInfo.language = newStoryInfoJSON.getString("language");
        newStoryInfo.series = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("series"));
        newStoryInfo.collections = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("collections"));
        newStoryInfo.published = LocalDate.parse(newStoryInfoJSON.getString("published"));
        newStoryInfo.status = newStoryInfoJSON.getString("status");
        newStoryInfo.statusWhen = LocalDate.parse(newStoryInfoJSON.getString("statusWhen"));
        newStoryInfo.words = newStoryInfoJSON.getInt("words");
        newStoryInfo.currentChapters = newStoryInfoJSON.getInt("currentChapters");
        newStoryInfo.totalChapters = newStoryInfoJSON.getInt("totalChapters");
        newStoryInfo.comments = newStoryInfoJSON.getInt("comments");
        newStoryInfo.kudos = newStoryInfoJSON.getInt("kudos");
        newStoryInfo.bookmarks = newStoryInfoJSON.getInt("bookmarks");
        newStoryInfo.hits = newStoryInfoJSON.getInt("hits");
        newStoryInfo.title = newStoryInfoJSON.getString("title");
        newStoryInfo.authors = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("authors"));
        newStoryInfo.summary = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("summary"));
        newStoryInfo.associations = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("associations"));
        newStoryInfo.startNotes = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("startNotes"));
        newStoryInfo.endNotes = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("endNotes"));
        newStoryInfo.registeredKudos = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("registeredKudos"));
        newStoryInfo.unnamedRegisteredKudos = newStoryInfoJSON.getInt("unnamedRegisteredKudos");
        newStoryInfo.guestKudos = newStoryInfoJSON.getInt("guestKudos");
        newStoryInfo.publicBookmarks = convertJSONArrayToArrayList(newStoryInfoJSON.getJSONArray("publicBookmarks"));

        return newStoryInfo;
    }
}
