package com.laoluade.ingestor.ao3.errors;

public class IngestorCanceledError extends Exception {
    public IngestorCanceledError() {
        super("Ingestion was canceled by parent instance.");
    }
}
