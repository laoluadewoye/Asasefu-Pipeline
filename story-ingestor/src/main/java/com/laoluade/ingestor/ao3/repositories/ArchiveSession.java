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
public class ArchiveSession {
    // Create ID field
    @Id
    private String id;

    // Create content fields
    private String sessionNickname;
    private String sessionCreated;
    private String sessionUpdated;
    private boolean sessionFinished;
    private boolean sessionCanceled;
    private boolean sessionException;
    private boolean sessionPurged;

    // Join the tables
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "parseId", referencedColumnName = "id")
    private ArchiveParse parseEntity;
}
