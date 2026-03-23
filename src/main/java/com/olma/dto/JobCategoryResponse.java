package com.olma.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JobCategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private List<JobCategoryResponse> children;
}
