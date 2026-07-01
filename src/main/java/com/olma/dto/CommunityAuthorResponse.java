package com.olma.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommunityAuthorResponse {
    private Long id;
    private String nickname;
    private String jobCategoryName;
    private String experienceLevelLabel;
    private String badgeLabel;
}
