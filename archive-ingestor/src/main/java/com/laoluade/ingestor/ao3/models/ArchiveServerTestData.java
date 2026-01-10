package com.laoluade.ingestor.ao3.models;

import lombok.Data;

/**
 * <p>This class defines the POJO model for archive server test data.</p>
 */
@Data
public class ArchiveServerTestData {
    /**
     * <p>This attribute holds the test string to send out to clients.</p>
     */
    private final String testData;
}
