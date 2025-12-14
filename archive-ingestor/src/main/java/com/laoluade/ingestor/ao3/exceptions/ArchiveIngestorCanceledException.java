package com.laoluade.ingestor.ao3.exceptions;

public class ArchiveIngestorCanceledException extends Exception {
    public ArchiveIngestorCanceledException() {
        super("Ingestion was canceled by parent instance.");
    }
}
