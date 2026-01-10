package com.laoluade.ingestor.ao3.exceptions;

/**
 * <p>This exception is used to indicate when the archive ingestor detects an incompatible AO3 website version.</p>
 */
public class ArchiveVersionIncompatibleException extends RuntimeException {
    /**
     * <p>
     *     This constructor creates the exception using the detected AO3 website version to send an alert message.
     * </p>
     * @param version The detected AO3 website version.
     */
    public ArchiveVersionIncompatibleException(String version) {
        super("The detected AO3 version " + version + " is not compatible with the current version of this program.");
    }
}
