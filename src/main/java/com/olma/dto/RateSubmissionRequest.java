package com.olma.dto;

import com.olma.domain.enums.AmountUnit;
import com.olma.domain.enums.SubmissionType;
import com.olma.domain.enums.WorkFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "단가 제보 생성 요청")
public class RateSubmissionRequest {

    @NotNull
    @Schema(description = "직무 카테고리 ID", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long jobCategoryId;

    @NotNull
    @Schema(description = "경력 레벨 ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long experienceLevelId;

    @Schema(description = "제보자 ID. 생략하면 JWT에서 추출한 userId를 사용합니다.", example = "1")
    private Long userId;

    @NotNull
    @Schema(description = "제보 유형", example = "TRACK_A", allowableValues = {"TRACK_A", "TRACK_B"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private SubmissionType submissionType;

    @NotNull
    @Schema(description = "근무 형태", example = "REMOTE", allowableValues = {"ON_SITE", "REMOTE", "HYBRID"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private WorkFormat workFormat;

    @Size(max = 50)
    @Schema(description = "프로젝트 기간. TOTAL 금액일 때 월 단가 환산에 사용합니다.", example = "2~3개월", maxLength = 50)
    private String duration;

    @NotNull
    @Min(10)
    @Schema(description = "제보 금액. 단위는 amountUnit으로 구분합니다.", example = "450",
            minimum = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer amount;

    @NotNull
    @Schema(description = "금액 단위", example = "MONTHLY", allowableValues = {"MONTHLY", "TOTAL"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private AmountUnit amountUnit;

    @NotNull
    @Schema(description = "익명/세션 단위 제보를 구분하기 위한 클라이언트 세션 ID",
            example = "8f14e45f-ea1d-4a7a-9b1f-4f5b9f4a7a01", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID sessionId;

    @Size(max = 100)
    @Schema(description = "사용자가 지정한 프로젝트명. 비어 있으면 생성일 기반 기본 이름을 사용합니다.", example = "쇼핑몰 관리자 페이지 구축", maxLength = 100)
    private String projectName;
}
