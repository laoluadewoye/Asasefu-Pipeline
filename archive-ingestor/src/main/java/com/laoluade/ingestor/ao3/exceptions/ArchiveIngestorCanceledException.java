package com.laoluade.ingestor.ao3.exceptions;

/**
 * <p>This exception is used to indicate when a parse session is canceled.</p>
 */
public class ArchiveIngestorCanceledException extends Exception {
    /**
     * <p>
     *     This constructor creates the exception by returning the string,
     *     "Ingestion was canceled by parent instance."
     * </p>
     */
    public ArchiveIngestorCanceledException() {
        super("Ingestion was canceled by parent instance.");
    }
}
