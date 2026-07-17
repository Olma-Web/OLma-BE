package com.olma.dto;

import com.olma.domain.enums.AmountUnit;
import com.olma.domain.enums.SubmissionType;
import com.olma.domain.enums.WorkFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
@Schema(description = "단가 제보 응답")
public class RateSubmissionResponse {
    @Schema(description = "단가 제보 ID", example = "1")
    private Long id;

    @Schema(description = "프로젝트명", example = "쇼핑몰 관리자 페이지 구축")
    private String projectName;

    @Schema(description = "직무 카테고리명", example = "백엔드 개발")
    private String jobCategoryName;

    @Schema(description = "경력 레벨 라벨", example = "3~5년")
    private String experienceLevelLabel;

    @Schema(description = "제보 유형", example = "TRACK_A")
    private SubmissionType submissionType;

    @Schema(description = "근무 형태", example = "REMOTE")
    private WorkFormat workFormat;

    @Schema(description = "프로젝트 기간", example = "2~3개월")
    private String duration;

    @Schema(description = "제보 금액", example = "450")
    private Integer amount;

    @Schema(description = "금액 단위", example = "MONTHLY")
    private AmountUnit amountUnit;

    @Schema(description = "월 단가로 환산한 금액. 환산할 수 없으면 null입니다.", example = "450")
    private Integer normalizedMonthly;

    @Schema(description = "제보 생성 시각", example = "2026-07-16T14:30:00+09:00")
    private OffsetDateTime createdAt;
}
