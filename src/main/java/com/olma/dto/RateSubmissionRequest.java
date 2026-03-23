package com.olma.dto;

import com.olma.domain.enums.AmountUnit;
import com.olma.domain.enums.Complexity;
import com.olma.domain.enums.SubmissionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class RateSubmissionRequest {

    @NotNull
    private Long jobCategoryId;

    @NotNull
    private Long workTypeId;

    private Long regionId;

    @NotNull
    private Long experienceLevelId;

    @NotNull
    private SubmissionType submissionType;

    @NotNull
    private Boolean isRemote;

    private Complexity complexity = Complexity.MEDIUM;

    @NotNull
    @Positive
    private BigDecimal durationMonths;

    @NotNull
    @PositiveOrZero
    private Integer amount;

    @NotNull
    private AmountUnit amountUnit;

    @NotNull
    private UUID sessionId;
}
