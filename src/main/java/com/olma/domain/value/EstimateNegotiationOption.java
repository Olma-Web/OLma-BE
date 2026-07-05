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
public class EstimateNegotiationOption {
    private String type;
    private String title;
    private Integer adjustedAmount;
    private Integer savingAmount;
    private Integer gapAfterAdjustment;
    private Integer adjustedScreenCount;
    private String uxEngagement;
    private List<String> addons;
    private List<String> adjustments;
    private String clientMessage;
}
