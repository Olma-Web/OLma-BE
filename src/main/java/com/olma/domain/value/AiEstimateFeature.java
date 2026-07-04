package com.olma.domain.value;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiEstimateFeature {
    private String name;
    private String description;
    private String complexity;
    private Integer minDays;
    private Integer expectedDays;
    private Integer maxDays;
    private String role;
    private String deliverable;
    private Integer amount;
}
