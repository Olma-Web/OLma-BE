package com.olma.dto;

import com.olma.domain.enums.CommunityCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class CommunityMyCommentResponse {
    private Long id;
    private Long postId;
    private String postTitle;
    private CommunityCategory postCategory;
    private Long parentCommentId;
    private String content;
    private OffsetDateTime createdAt;
}
