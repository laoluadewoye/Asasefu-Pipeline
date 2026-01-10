package com.laoluade.ingestor.ao3.exceptions;

import org.openqa.selenium.NoSuchElementException;

/**
 * <p>
 *     This exception is used to indicate when a {@link NoSuchElementException} is thrown by the
 *     {@link com.laoluade.ingestor.ao3.core.ArchiveIngestor}.
 * </p>
 */
public class ArchiveElementNotFoundException extends Exception {
    /**
     * <p>This constructor creates the exception to throw using the driver's current link and the exception contents.</p>
     * @param currentURL The current link that the driver is on.
     * @param e The {@link NoSuchElementException} thrown.
     */
    public ArchiveElementNotFoundException(String currentURL, NoSuchElementException e) {
        String smallE = e.toString().split("\n")[0];
        String errorMessage = "The Story Link " + currentURL + " returned the following message:\n\t" + smallE;
        super(errorMessage);
    }
}
