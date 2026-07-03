package com.olma.domain.repository;

import com.olma.domain.entity.CommunityPost;
import com.olma.domain.enums.CommunityCategory;
import com.olma.domain.enums.CommunityContentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    Page<CommunityPost> findAllByStatusOrderByCreatedAtDesc(CommunityContentStatus status, Pageable pageable);

    Page<CommunityPost> findAllByStatusAndCategoryOrderByCreatedAtDesc(
            CommunityContentStatus status,
            CommunityCategory category,
            Pageable pageable
    );

    Page<CommunityPost> findAllByAuthor_IdAndStatusOrderByCreatedAtDesc(
            Long authorId,
            CommunityContentStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM CommunityPost p
            JOIN FETCH p.author a
            LEFT JOIN FETCH a.jobCategory
            LEFT JOIN FETCH a.experienceLevel
            WHERE p.id = :id AND p.status = :status
            """)
    Optional<CommunityPost> findActiveByIdWithAuthor(
            @Param("id") Long id,
            @Param("status") CommunityContentStatus status
    );

    @Query("""
            SELECT p
            FROM CommunityPost p
            WHERE p.status = :status
              AND (:category IS NULL OR p.category = :category)
              AND p.createdAt >= :from
              AND p.likeCount > 0
            ORDER BY p.likeCount DESC, p.createdAt DESC
            """)
    List<CommunityPost> findWeeklyBest(
            @Param("status") CommunityContentStatus status,
            @Param("category") CommunityCategory category,
            @Param("from") OffsetDateTime from,
            Pageable pageable
    );
}
