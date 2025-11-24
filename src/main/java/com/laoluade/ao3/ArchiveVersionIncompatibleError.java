package com.laoluade.ao3;

public class ArchiveVersionIncompatibleError extends RuntimeException {
    public ArchiveVersionIncompatibleError(String version) {
        super("The detected AO3 version " + version + " is not compatible with the current version of this program.");
    }
}
