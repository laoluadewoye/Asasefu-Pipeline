package com.laoluade.ingestor.ao3.exceptions;

public class ArchiveParagraphsNotFoundException extends Exception {
    public ArchiveParagraphsNotFoundException() {
        super("Chapter text was not found in expected places on story page.");
    }
}
