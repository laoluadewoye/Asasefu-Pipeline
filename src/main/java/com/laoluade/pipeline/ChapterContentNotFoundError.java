package com.laoluade.pipeline;

public class ChapterContentNotFoundError extends RuntimeException {
    public ChapterContentNotFoundError() {
        super("Chapter text was not found in expected places on story page.");
    }
}
