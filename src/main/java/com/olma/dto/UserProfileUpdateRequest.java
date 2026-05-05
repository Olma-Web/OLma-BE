package com.olma.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserProfileUpdateRequest {
    private Long jobCategoryId;
    private Long experienceLevelId;
    private List<Long> certificateTypeIds;
}
