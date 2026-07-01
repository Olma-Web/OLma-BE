package com.olma.domain.repository;

import com.olma.domain.entity.CommunityPostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, Long> {

    Optional<CommunityPostLike> findByPost_IdAndUser_Id(Long postId, Long userId);
}
