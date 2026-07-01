package com.olma.dto;

import com.olma.domain.enums.CommunityCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class CommunityPostDetailResponse {
    private Long id;
    private CommunityCategory category;
    private String title;
    private String content;
    private CommunityAuthorResponse author;
    private List<String> imageUrls;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean likedByMe;
    private OffsetDateTime createdAt;
    private List<CommunityCommentResponse> comments;
}
