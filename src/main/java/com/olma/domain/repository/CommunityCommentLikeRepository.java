package com.olma.domain.repository;

import com.olma.domain.entity.CommunityCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityCommentLikeRepository extends JpaRepository<CommunityCommentLike, Long> {

    Optional<CommunityCommentLike> findByComment_IdAndUser_Id(Long commentId, Long userId);
}
