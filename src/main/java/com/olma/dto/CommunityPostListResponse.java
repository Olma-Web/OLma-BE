package com.olma.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CommunityPostListResponse {
    private List<CommunityPostSummaryResponse> bestPosts;
    private List<CommunityPostSummaryResponse> posts;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
