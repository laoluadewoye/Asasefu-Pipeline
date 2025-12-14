package com.laoluade.ingestor.ao3.exceptions;

public class ArchiveVersionIncompatibleException extends RuntimeException {
    public ArchiveVersionIncompatibleException(String version) {
        super("The detected AO3 version " + version + " is not compatible with the current version of this program.");
    }
}
