package com.olma.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.olma.domain.enums.ProfileSpecStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class UserProfileResponse {
    private Long id;
    private String email;
    private String nickname;
    private Long jobCategoryId;
    private String jobCategoryName;
    private Long experienceLevelId;
    private String experienceLevelLabel;
    private List<CertificateInfo> certificates;
    private ProfileSpecStatus profileSpecStatus;
    private JsonNode profileSpecState;
    private OffsetDateTime profileSpecStartedAt;
    private OffsetDateTime profileSpecUpdatedAt;
    private OffsetDateTime profileSpecCompletedAt;

    @Getter
    @Builder
    public static class CertificateInfo {
        private Long id;
        private String name;
    }
}
