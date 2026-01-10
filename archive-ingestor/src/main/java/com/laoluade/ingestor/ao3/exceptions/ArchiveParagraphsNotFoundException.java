package com.laoluade.ingestor.ao3.exceptions;

/**
 * <p>This exception is used to indicate when the archive ingestor cannot find the body of a story/chapter.</p>
 */
public class ArchiveParagraphsNotFoundException extends Exception {
    /**
     * <p>
     *     This constructor creates the exception by returning the string,
     *     "Chapter text was not found in expected places on story page."
     * </p>
     */
    public ArchiveParagraphsNotFoundException() {
        super("Chapter text was not found in expected places on story page.");
    }
}
