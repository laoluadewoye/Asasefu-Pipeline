package com.laoluade.ingestor.ao3.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>This interface defines extra methods for interacting with the table for {@link ArchiveSession} entities.</p>
 */
public interface ArchiveSessionRepository extends JpaRepository<ArchiveSession, String> {
    /**
     * <p>This method defines the transaction for updating the update timestamp in an {@link ArchiveSession} entity.</p>
     * @param sessionId The primary key of the {@link ArchiveSession} entity.
     * @param timestamp The new timestamp to enter.
     */
    @Modifying(flushAutomatically = true)
    @Transactional
    @Query("UPDATE ArchiveSession s SET s.sessionUpdated = :timestamp WHERE s.id = :sessionId")
    void updateSessionUpdatedTimestamp(@Param("sessionId") String sessionId, @Param("timestamp") String timestamp);

    /**
     * <p>This method defines the transaction for updating the last message in an {@link ArchiveSession} entity.</p>
     * @param sessionId The primary key of the {@link ArchiveSession} entity.
     * @param newLastMessage The new message to enter.
     */
    @Modifying(flushAutomatically = true)
    @Transactional
    @Query("UPDATE ArchiveSession s SET s.sessionLastMessage = :newLastMessage WHERE s.id = :sessionId")
    void updateLastMessage(@Param("sessionId") String sessionId, @Param("newLastMessage") String newLastMessage);

    /**
     * <p>This method defines the transaction for setting the finished flag of an {@link ArchiveSession} entity.</p>
     * @param sessionId The primary key of the {@link ArchiveSession} entity.
     */
    @Modifying(flushAutomatically = true)
    @Transactional
    @Query("UPDATE ArchiveSession s SET s.sessionFinished = true WHERE s.id = :sessionId")
    void updateFinishedStatus(@Param("sessionId") String sessionId);

    /**
     * <p>This method defines the transaction for setting the canceled flag of an {@link ArchiveSession} entity.</p>
     * @param sessionId The primary key of the {@link ArchiveSession} entity.
     */
    @Modifying(flushAutomatically = true)
    @Transactional
    @Query("UPDATE ArchiveSession s SET s.sessionCanceled = true WHERE s.id = :sessionId")
    void updateCanceledStatus(@Param("sessionId") String sessionId);

    /**
     * <p>This method defines the transaction for setting the exception flag of an {@link ArchiveSession} entity.</p>
     * @param sessionId The primary key of the {@link ArchiveSession} entity.
     */
    @Modifying(flushAutomatically = true)
    @Transactional
    @Query("UPDATE ArchiveSession s SET s.sessionException = true WHERE s.id = :sessionId")
    void updateExceptionStatus(@Param("sessionId") String sessionId);

    /**
     * <p>This method defines the transaction for setting the purged flag of an {@link ArchiveSession} entity.</p>
     * @param sessionId The primary key of the {@link ArchiveSession} entity.
     */
    @Modifying(flushAutomatically = true)
    @Transactional
    @Query("UPDATE ArchiveSession s SET s.sessionPurged = true WHERE s.id = :sessionId")
    void updatePurgedStatus(@Param("sessionId") String sessionId);
}
