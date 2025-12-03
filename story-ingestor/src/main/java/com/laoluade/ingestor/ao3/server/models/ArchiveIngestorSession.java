package com.laoluade.ingestor.ao3.server.models;

import java.util.concurrent.CompletableFuture;

public class ArchiveIngestorSession {
    private ArchiveIngestorResponse sessionResponse;
    private CompletableFuture<ArchiveIngestorTaskFuture> sessionFuture;

    public ArchiveIngestorSession(ArchiveIngestorResponse sessionResponse,
                                  CompletableFuture<ArchiveIngestorTaskFuture> sessionFuture) {
        this.sessionResponse = sessionResponse;
        this.sessionFuture = sessionFuture;
    }

    public void setSessionResponse(ArchiveIngestorResponse sessionResponse) { this.sessionResponse = sessionResponse; }

    public ArchiveIngestorResponse getSessionResponse() { return this.sessionResponse; }

    public void setSessionFuture(CompletableFuture<ArchiveIngestorTaskFuture> sessionFuture) {
        this.sessionFuture = sessionFuture;
    }

    public CompletableFuture<ArchiveIngestorTaskFuture> getSessionFuture() { return this.sessionFuture; }
}
