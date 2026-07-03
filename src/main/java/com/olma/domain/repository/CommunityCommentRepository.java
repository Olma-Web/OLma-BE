package com.olma.domain.repository;

import com.olma.domain.entity.CommunityComment;
import com.olma.domain.enums.CommunityContentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    List<CommunityComment> findAllByPost_IdAndStatusOrderByCreatedAtAsc(Long postId, CommunityContentStatus status);

    List<CommunityComment> findAllByParentComment_IdAndStatus(Long parentCommentId, CommunityContentStatus status);

    @Query(
            value = """
                    SELECT c
                    FROM CommunityComment c
                    JOIN FETCH c.post p
                    WHERE c.author.id = :authorId
                      AND c.status = :status
                      AND p.status = :postStatus
                    ORDER BY c.createdAt DESC
                    """,
            countQuery = """
                    SELECT count(c)
                    FROM CommunityComment c
                    JOIN c.post p
                    WHERE c.author.id = :authorId
                      AND c.status = :status
                      AND p.status = :postStatus
                    """
    )
    Page<CommunityComment> findMyActiveComments(
            @Param("authorId") Long authorId,
            @Param("status") CommunityContentStatus status,
            @Param("postStatus") CommunityContentStatus postStatus,
            Pageable pageable
    );
}
