package com.olma.dto;

import lombok.Builder;
import lombok.Getter;

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

    @Getter
    @Builder
    public static class CertificateInfo {
        private Long id;
        private String name;
    }
}
