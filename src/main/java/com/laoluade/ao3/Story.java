package com.laoluade.ao3;

import com.google.common.hash.Hashing;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class Story {
    // Meta items - self created
    public ZonedDateTime creationTimestamp;
    public String creationHash;

    // Meta items - passed in
    public StoryInfo storyInfo;
    public ArrayList<Chapter> chapters;

    public Story(StoryInfo storyInfo, ArrayList<Chapter> chapters) {
        // Get current timestamp
        this.creationTimestamp = ZonedDateTime.now(ZoneId.of("UTC"));

        // Set information
        this.storyInfo = storyInfo;
        this.chapters = chapters;

        // Generate the hash using hashes from story information and chapters
        String chapterHashes = "";
        for (Chapter chapter : this.chapters) {
            chapterHashes = chapterHashes + chapter.creationHash;
        }
        String creationHashInput = this.storyInfo.creationHash + chapterHashes;
        this.creationHash = Hashing.sha256().hashString(creationHashInput, StandardCharsets.UTF_8).toString();
    }

    public JSONObject getJSONRep() {
        JSONArray chapterJSONs = new JSONArray();
        for (Chapter chapter : this.chapters) {
            chapterJSONs.put(chapter.getJSONRepWithoutParent());
        }

        JSONObject rep = new JSONObject();
        rep.put("creationTimestamp", this.creationTimestamp.toString());
        rep.put("creationHash", this.creationHash);
        rep.put("storyInfo", this.storyInfo.getJSONRep());
        rep.put("chapters", chapterJSONs);
        return rep;
    }
}
