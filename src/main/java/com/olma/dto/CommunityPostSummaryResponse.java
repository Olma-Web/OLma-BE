package com.olma.dto;

import com.olma.domain.enums.CommunityCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class CommunityPostSummaryResponse {
    private Long id;
    private CommunityCategory category;
    private String title;
    private String contentPreview;
    private CommunityAuthorResponse author;
    private List<String> imageUrls;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean best;
    private OffsetDateTime createdAt;
}
