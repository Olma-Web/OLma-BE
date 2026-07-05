package com.olma.domain.value;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstimateNegotiationResult {
    private String status;
    private Integer currentAmount;
    private Integer targetBudgetAmount;
    private Integer gapAmount;
    private String recommendedOptionType;
    private List<EstimateNegotiationOption> options;
    private String clientMessage;
}
