package com.laoluade.ingestor.ao3.repositories;

import com.laoluade.ingestor.ao3.models.ArchiveIngestorResponse;
import com.laoluade.ingestor.ao3.models.ArchiveIngestorTaskFuture;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.concurrent.CompletableFuture;

@Entity
public class ArchiveIngestorSessionEntity {
    // Create ID field
    @Id
    private String id;

    // Create content fields
    private CompletableFuture<ArchiveIngestorTaskFuture> sessionFuture;
    private ArchiveIngestorResponse sessionResponse;

    protected ArchiveIngestorSessionEntity() {}

    public ArchiveIngestorSessionEntity(String id, CompletableFuture<ArchiveIngestorTaskFuture> sessionFuture,
                                        ArchiveIngestorResponse sessionResponse) {
        super();
        this.id = id;
        this.sessionFuture = sessionFuture;
        this.sessionResponse = sessionResponse;
    }

    public void setId(String id) { this.id = id; }

    public String getId() { return this.id; }

    public void setSessionResponse(ArchiveIngestorResponse sessionResponse) { this.sessionResponse = sessionResponse; }

    public ArchiveIngestorResponse getSessionResponse() { return this.sessionResponse; }

    public void setSessionFuture(CompletableFuture<ArchiveIngestorTaskFuture> sessionFuture) {
        this.sessionFuture = sessionFuture;
    }

    public CompletableFuture<ArchiveIngestorTaskFuture> getSessionFuture() { return this.sessionFuture; }
}
