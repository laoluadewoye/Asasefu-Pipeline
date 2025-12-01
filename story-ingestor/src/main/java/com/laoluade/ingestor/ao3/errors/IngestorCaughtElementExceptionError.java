package com.laoluade.ingestor.ao3.errors;

import org.openqa.selenium.NoSuchElementException;

public class IngestorCaughtElementExceptionError extends RuntimeException {
    public IngestorCaughtElementExceptionError(String currentURL, NoSuchElementException e) {
        String smallE = e.toString().split("\n")[0];
        String errorMessage = "The Story Link " + currentURL + " returned the following message:\n\t" + smallE;
        super(errorMessage);
    }
}
