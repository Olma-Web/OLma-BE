package com.olma.dto;

import com.olma.domain.value.AiEstimateBreakdown;
import com.olma.domain.value.AiEstimateFeature;
import com.olma.domain.value.AiEstimateRisk;
import com.olma.domain.value.AiEstimateSchedule;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class AiEstimateResponse {
    private Long savedEstimateId;
    private String projectName;
    private String projectDescription;
    private String platform;
    private Integer estimatedScreenCount;
    private List<AiEstimateFeature> features;
    private Integer totalExpectedDays;
    private Integer finalAmount;
    private AiEstimateSchedule schedule;
    private List<AiEstimateRisk> risks;
    private List<AiEstimateBreakdown> breakdown;
    private String clientMessage;
    private OffsetDateTime createdAt;
}
