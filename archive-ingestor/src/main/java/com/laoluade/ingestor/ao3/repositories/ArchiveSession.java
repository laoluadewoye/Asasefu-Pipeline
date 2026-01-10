package com.laoluade.ingestor.ao3.repositories;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

/**
 * <p>This class defines the JPA entity for an archive server session.</p>
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArchiveSession {
    // Create ID field
    /**
     * <p>This attribute is the primary key for the entity.</p>
     */
    @Id
    private String id;

    // Create content fields
    /**
     * <p>This attribute describes the session nickname of the entity.</p>
     */
    private String sessionNickname;

    /**
     * <p>This attribute describes the timestamp the session was created.</p>
     */
    private String sessionCreated;

    /**
     * <p>This attribute describes the timestamp the session was last updated.</p>
     */
    private String sessionUpdated;

    /**
     * <p>This attribute describes whether the session finished successfully.</p>
     */
    private boolean sessionFinished;

    /**
     * <p>This attribute describes whether the session was canceled by the client.</p>
     */
    private boolean sessionCanceled;

    /**
     * <p>This attribute describes whether the session's activities experienced an exception.</p>
     */
    private boolean sessionException;

    /**
     * <p>This attribute describes whether the session has been purged by the session service.</p>
     */
    private boolean sessionPurged;

    // Create a custom result column
    /**
     * <p>This attribute describes the message last added to the session entity.</p>
     */
    @Column(columnDefinition = "TEXT")
    private String sessionLastMessage;

    // Join the tables
    /**
     * <p>This attribute describes the cascading join to the {@link ArchiveParse} table using the ids of parse entities.</p>
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "parseId", referencedColumnName = "id")
    private ArchiveParse parseEntity;
}
