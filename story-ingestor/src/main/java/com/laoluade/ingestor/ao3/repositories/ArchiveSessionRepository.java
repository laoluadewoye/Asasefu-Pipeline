package com.laoluade.ingestor.ao3.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArchiveSessionRepository extends JpaRepository<ArchiveSession, String> {
    @Modifying(flushAutomatically = true)
    @Query("UPDATE ArchiveSession s SET s.sessionUpdated = :timestamp WHERE s.id = :sessionId")
    void updateSessionUpdatedTimestamp(@Param("sessionId") String sessionId, @Param("timestamp") String timestamp);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE ArchiveSession s SET s.sessionFinished = true WHERE s.id = :sessionId")
    void updateFinishedStatus(@Param("sessionId") String sessionId);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE ArchiveSession s SET s.sessionCanceled = true WHERE s.id = :sessionId")
    void updateCanceledStatus(@Param("sessionId") String sessionId);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE ArchiveSession s SET s.sessionException = true WHERE s.id = :sessionId")
    void updateExceptionStatus(@Param("sessionId") String sessionId);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE ArchiveSession s SET s.sessionPurged = true WHERE s.id = :sessionId")
    void updatePurgedStatus(@Param("sessionId") String sessionId);
}
