package com.olma.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstimateNegotiationRequest extends EstimateCalculateRequest {

    @NotNull
    @Positive
    private Integer targetBudgetAmount;
}
