package com.olma.dto;

import lombok.Builder;
import lombok.Getter;

import com.olma.domain.value.EstimateNegotiationResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class EstimateCalculateResponse {
    private Long savedEstimateId;
    private String projectName;
    private String experienceLevelLabel;
    private String jobCategoryName;
    private Integer baseDailyRate;
    private Integer screenCount;
    private Integer step1BasicFee;
    private BigDecimal uxMultiplier;
    private Integer step2UxFee;
    private BigDecimal platformMultiplier;
    private Integer step3PlatformFee;
    private List<String> addons;
    private Integer addonPercent;
    private Integer step4AddonFee;
    private Integer finalAmount;
    private EstimateNegotiationResult negotiationResult;
    private OffsetDateTime createdAt;
}
