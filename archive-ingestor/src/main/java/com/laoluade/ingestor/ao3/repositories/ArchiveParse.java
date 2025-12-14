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
    private int parseChaptersCompleted;
    private int parseChaptersTotal;

    // Create a custom result column
    @Column(columnDefinition = "TEXT")
    private String parseResult;

    public ArchiveParse(ArchiveParseType parseType, String parseTargetLink, int parseChaptersCompleted,
                        int parseChaptersTotal, String parseResult) {
        this.parseType = parseType;
        this.parseTargetLink = parseTargetLink;
        this.parseChaptersCompleted = parseChaptersCompleted;
        this.parseChaptersTotal = parseChaptersTotal;
        this.parseResult = parseResult;
    }
}
