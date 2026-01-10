package com.laoluade.ingestor.ao3.exceptions;

/**
 * <p>This exception is used to indicate when the archive ingestor cannot find the requested story page.</p>
 */
public class ArchivePageNotFoundException extends Exception {
    /**
     * <p>
     *     This constructor creates the exception by returning the string,
     *     "404 Page was reached and ingestor stopped parsing."
     * </p>
     */
    public ArchivePageNotFoundException() {
        super("404 Page was reached and ingestor stopped parsing.");
    }
}
