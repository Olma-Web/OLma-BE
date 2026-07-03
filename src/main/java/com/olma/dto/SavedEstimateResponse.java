package com.olma.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class SavedEstimateResponse {
    private Long id;
    private String projectName;
    private String experienceLevelLabel;
    private String jobCategoryName;
    private Integer screenCount;
    private BigDecimal uxMultiplier;
    private BigDecimal platformMultiplier;
    private List<String> addons;
    private Integer addonPercent;
    private Integer finalAmount;
    private OffsetDateTime createdAt;
}
