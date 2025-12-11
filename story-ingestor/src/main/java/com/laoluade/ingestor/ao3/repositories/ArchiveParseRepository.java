package com.laoluade.ingestor.ao3.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArchiveParseRepository extends JpaRepository<ArchiveParse, String> {
    @Modifying(flushAutomatically = true)
    @Query("UPDATE ArchiveParse p SET p.parseChaptersTotal = :chapterCount WHERE p.id = :id")
    void updateChaptersTotal(@Param("id") Long id, @Param("chapterCount") Integer chapterCount);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE ArchiveParse p SET p.parseChaptersCompleted = :chapterCount WHERE p.id = :id")
    void updateChaptersCompleted(@Param("id") Long id, @Param("chapterCount") Integer chapterCount);
}
