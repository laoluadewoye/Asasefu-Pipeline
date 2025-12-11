package com.laoluade.ingestor.ao3.repositories;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArchiveParse {
    // Create ID field
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // Create content fields
    private ArchiveParseType parseType;
    private String parseTargetLink;
    private String parseResult;
    private int parseChaptersCompleted;
    private int parseChaptersTotal;

    public ArchiveParse(ArchiveParseType parseType, String parseTargetLink, String parseResult,
                        int parseChaptersCompleted, int parseChaptersTotal) {
        this.parseType = parseType;
        this.parseTargetLink = parseTargetLink;
        this.parseResult = parseResult;
        this.parseChaptersCompleted = parseChaptersCompleted;
        this.parseChaptersTotal = parseChaptersTotal;
    }
}
