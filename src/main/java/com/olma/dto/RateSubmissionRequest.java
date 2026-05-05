package com.olma.dto;

import com.olma.domain.enums.AmountUnit;
import com.olma.domain.enums.SubmissionType;
import com.olma.domain.enums.WorkFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RateSubmissionRequest {

    @NotNull
    private Long jobCategoryId;

    @NotNull
    private Long experienceLevelId;

    private Long userId;

    @NotNull
    private SubmissionType submissionType;

    @NotNull
    private WorkFormat workFormat;

    @Size(max = 50)
    private String duration;

    @NotNull
    @PositiveOrZero
    private Integer amount;

    @NotNull
    private AmountUnit amountUnit;

    @NotNull
    private UUID sessionId;
}
