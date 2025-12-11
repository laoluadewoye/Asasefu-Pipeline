package com.laoluade.ingestor.ao3.exceptions;

import org.openqa.selenium.NoSuchElementException;

public class ArchiveElementNotFoundException extends Exception {
    public ArchiveElementNotFoundException(String currentURL, NoSuchElementException e) {
        String smallE = e.toString().split("\n")[0];
        String errorMessage = "The Story Link " + currentURL + " returned the following message:\n\t" + smallE;
        super(errorMessage);
    }
}
