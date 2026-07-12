package com.olma.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.olma.domain.enums.NegotiationSimulationStatus;
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
    private Long experienceLevelId;
    private String experienceLevelLabel;
    private Long jobCategoryId;
    private String jobCategoryName;
    private Integer baseAmount;
    private Integer screenCount;
    private BigDecimal uxMultiplier;
    private BigDecimal platformMultiplier;
    private List<String> addons;
    private Integer addonPercent;
    private Integer finalAmount;
    private OffsetDateTime createdAt;
    private boolean negotiationSimulationStarted;
    private NegotiationSimulationStatus negotiationSimulationStatus;
    private JsonNode negotiationSimulationState;
    private OffsetDateTime negotiationSimulationStartedAt;
    private OffsetDateTime negotiationSimulationUpdatedAt;
    private OffsetDateTime negotiationSimulationCompletedAt;
}
