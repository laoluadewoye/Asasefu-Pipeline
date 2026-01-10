package com.laoluade.ingestor.ao3.models;

import lombok.Data;

/**
 * <p>This class defines the POJO model for completable future data for the Archive Server.</p>
 */
@Data
public class ArchiveServerFutureData {
    /**
     * <p>This attribute holds the result message for the completed asynchronous task.</p>
     */
    private final String resultMessage;

    /**
     * <p>This attribute holds the success flag for the completed asynchronous task.</p>
     */
    private final boolean isSuccess;
}
