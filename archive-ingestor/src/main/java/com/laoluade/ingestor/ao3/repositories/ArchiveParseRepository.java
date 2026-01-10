package com.laoluade.ingestor.ao3.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>This interface defines extra methods for interacting with the table for {@link ArchiveParse} entities.</p>
 */
public interface ArchiveParseRepository extends JpaRepository<ArchiveParse, String> {
    /**
     * <p>This method defines the transaction for updating the total chapters value in an {@link ArchiveParse} entity.</p>
     * @param id The primary key of the {@link ArchiveParse} entity.
     * @param chapterCount The new chapter count to enter.
     */
    @Modifying(flushAutomatically = true)
    @Transactional
    @Query("UPDATE ArchiveParse p SET p.parseChaptersTotal = :chapterCount WHERE p.id = :id")
    void updateChaptersTotal(@Param("id") Long id, @Param("chapterCount") Integer chapterCount);

    /**
     * <p>This method defines the transaction for updating the completed chapters value in an {@link ArchiveParse} entity.</p>
     * @param id The primary key of the {@link ArchiveParse} entity.
     * @param chapterCount The new chapter count to enter.
     */
    @Modifying(flushAutomatically = true)
    @Transactional
    @Query("UPDATE ArchiveParse p SET p.parseChaptersCompleted = :chapterCount WHERE p.id = :id")
    void updateChaptersCompleted(@Param("id") Long id, @Param("chapterCount") Integer chapterCount);
}
