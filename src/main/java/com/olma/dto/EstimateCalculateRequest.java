package com.olma.dto;

import com.olma.domain.enums.EstimateAddon;
import com.olma.domain.enums.PlatformEnvironment;
import com.olma.domain.enums.UxEngagement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EstimateCalculateRequest {

    @NotNull
    private Long experienceLevelId;

    @NotNull
    private Long jobCategoryId;

    @NotNull
    @Positive
    private Integer screenCount;

    @NotNull
    private UxEngagement uxEngagement;

    @NotNull
    private PlatformEnvironment platformEnvironment;

    private List<EstimateAddon> addons = new ArrayList<>();

    @Size(max = 100)
    private String projectName;

    @Positive
    private Integer negotiationTargetBudgetAmount;
}
