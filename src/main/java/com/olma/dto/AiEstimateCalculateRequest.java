package com.olma.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiEstimateCalculateRequest {

    @NotBlank
    @Size(min = 20, max = 3000)
    private String projectDescription;

    @Size(max = 100)
    private String platformHint;

    @Positive
    private Integer desiredTimelineDays;

    @Positive
    private Integer budgetAmount;

    @Size(max = 500)
    private String scopeHint;

    @Size(max = 100)
    private String projectName;
}
