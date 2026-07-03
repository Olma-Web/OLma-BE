package com.olma.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CommunityCommentListResponse {
    private List<CommunityMyCommentResponse> comments;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
