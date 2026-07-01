package com.olma.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class CommunityCommentResponse {
    private Long id;
    private Long parentCommentId;
    private String content;
    private CommunityAuthorResponse author;
    private OffsetDateTime createdAt;
    private List<CommunityCommentResponse> replies;
}
