package com.laoluade.ingestor.ao3.models;

import lombok.Data;

/**
 * <p>This class defines the POJO model for archive server specification data.</p>
 */
@Data
public class ArchiveServerSpecData {
    /**
     * <p>This attribute holds the version for the archive ingestor.</p>
     */
    private final String archiveIngestorVersion;

    /**
     * <p>This attribute holds the latest OTW Archive version supported by the archive ingestor.</p>
     */
    private final String latestOTWArchiveVersion;
}
