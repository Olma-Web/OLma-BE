package com.olma.dto;

import com.olma.domain.enums.AmountUnit;
import com.olma.domain.enums.Complexity;
import com.olma.domain.enums.SubmissionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class RateSubmissionResponse {
    private Long id;
    private String jobCategoryName;
    private String workTypeName;
    private String regionName;
    private String experienceLevelLabel;
    private SubmissionType submissionType;
    private Boolean isRemote;
    private Complexity complexity;
    private BigDecimal durationMonths;
    private Integer amount;
    private AmountUnit amountUnit;
    private Integer normalizedMonthly;
    private OffsetDateTime createdAt;
}
