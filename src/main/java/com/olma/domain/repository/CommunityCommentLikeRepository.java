package com.olma.domain.repository;

import com.olma.domain.entity.CommunityCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface CommunityCommentLikeRepository extends JpaRepository<CommunityCommentLike, Long> {

    Optional<CommunityCommentLike> findByComment_IdAndUser_Id(Long commentId, Long userId);

    @Query("select cl.comment.id from CommunityCommentLike cl where cl.user.id = :userId and cl.comment.id in :commentIds")
    Set<Long> findLikedCommentIds(@Param("userId") Long userId, @Param("commentIds") Collection<Long> commentIds);
}
