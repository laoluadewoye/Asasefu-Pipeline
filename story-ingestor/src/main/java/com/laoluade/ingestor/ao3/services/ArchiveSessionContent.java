package com.laoluade.ingestor.ao3.services;

import com.laoluade.ingestor.ao3.models.ArchiveServerFutureData;
import lombok.Data;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;

@Data
public class ArchiveSessionContent {
    @NonNull private CompletableFuture<ArchiveServerFutureData> future;
    @NonNull private String lastRecordedMessage;
}
