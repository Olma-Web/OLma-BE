package com.olma.domain.repository;

import com.olma.domain.entity.CommunityComment;
import com.olma.domain.enums.CommunityContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    List<CommunityComment> findAllByPost_IdAndStatusOrderByCreatedAtAsc(Long postId, CommunityContentStatus status);

    List<CommunityComment> findAllByParentComment_IdAndStatus(Long parentCommentId, CommunityContentStatus status);
}
