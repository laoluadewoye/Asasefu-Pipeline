package com.laoluade.ingestor.ao3.repositories;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

/**
 * <p>This class defines the JPA entity for an archive server parse.</p>
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArchiveParse {
    // Create ID field
    /**
     * <p>This attribute is the primary key for the entity.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // Create content fields
    /**
     * <p>This attribute describes what kind of parse the parse entry is.</p>
     */
    private ArchiveParseType parseType;

    /**
     * <p>This attribute describes the web link the parse is targeting.</p>
     */
    private String parseTargetLink;

    /**
     * <p>This attribute describes how many chapters the parse has finished analyzing.</p>
     */
    private int parseChaptersCompleted;

    /**
     * <p>This attribute describes the total number of chapters the parse needs to analyze.</p>
     */
    private int parseChaptersTotal;

    // Create a custom result column
    /**
     * <p>This attribute describes the JSON string of the finished parse.</p>
     */
    @Column(columnDefinition = "TEXT")
    private String parseResult;

    /**
     * <p>This constructor creates the parse entry in the <code>ArchiveParse</code> JPA table.</p>
     * @param parseType The kind of parse the parse entry is.
     * @param parseTargetLink The web link the parse is targeting.
     * @param parseChaptersCompleted How many chapters the parse has finished analyzing.
     * @param parseChaptersTotal The total number of chapters the parse needs to analyze.
     * @param parseResult The JSON string of the finished parse.
     */
    public ArchiveParse(ArchiveParseType parseType, String parseTargetLink, int parseChaptersCompleted,
                        int parseChaptersTotal, String parseResult) {
        this.parseType = parseType;
        this.parseTargetLink = parseTargetLink;
        this.parseChaptersCompleted = parseChaptersCompleted;
        this.parseChaptersTotal = parseChaptersTotal;
        this.parseResult = parseResult;
    }
}
