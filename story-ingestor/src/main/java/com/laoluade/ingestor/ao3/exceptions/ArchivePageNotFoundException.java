package com.laoluade.ingestor.ao3.exceptions;

public class ArchivePageNotFoundException extends Exception {
    public ArchivePageNotFoundException() {
        super("404 Page was reached and ingestor stopped parsing.");
    }
}
