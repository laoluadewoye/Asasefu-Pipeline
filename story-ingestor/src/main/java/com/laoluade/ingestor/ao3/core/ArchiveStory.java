package com.laoluade.ingestor.ao3.core;

import com.google.common.hash.Hashing;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class ArchiveStory {
    // Meta items - self created
    public ZonedDateTime creationTimestamp;
    public String creationHash;

    // Meta items - passed in
    public ArchiveStoryInfo archiveStoryInfo;
    public ArrayList<ArchiveChapter> archiveChapters;

    public ArchiveStory(ArchiveStoryInfo archiveStoryInfo, ArrayList<ArchiveChapter> archiveChapters) {
        // Get current timestamp
        this.creationTimestamp = ZonedDateTime.now(ZoneId.of("UTC"));

        // Set information
        this.archiveStoryInfo = archiveStoryInfo;
        this.archiveChapters = archiveChapters;

        // Generate the hash using hashes from story information and chapters
        String chapterHashes = "";
        for (ArchiveChapter archiveChapter : this.archiveChapters) {
            chapterHashes = chapterHashes + archiveChapter.creationHash;
        }
        String creationHashInput = this.archiveStoryInfo.creationHash + chapterHashes;
        this.creationHash = Hashing.sha256().hashString(creationHashInput, StandardCharsets.UTF_8).toString();
    }

    public JSONObject getJSONRep() {
        JSONArray chapterJSONs = new JSONArray();
        for (ArchiveChapter archiveChapter : this.archiveChapters) {
            chapterJSONs.put(archiveChapter.getJSONRepWithoutParent());
        }

        JSONObject rep = new JSONObject();
        rep.put("creationTimestamp", this.creationTimestamp.toString());
        rep.put("creationHash", this.creationHash);
        rep.put("archiveStoryInfo", this.archiveStoryInfo.getJSONRep());
        rep.put("archiveChapters", chapterJSONs);
        return rep;
    }
}
