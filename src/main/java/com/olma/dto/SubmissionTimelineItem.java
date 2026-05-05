package com.olma.dto;

import com.olma.domain.enums.AmountUnit;
import com.olma.domain.enums.SubmissionType;
import com.olma.domain.enums.WorkFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class SubmissionTimelineItem {
    private Long id;
    private SubmissionType submissionType;
    private WorkFormat workFormat;
    private String duration;
    private Integer amount;
    private AmountUnit amountUnit;
    private String jobCategoryName;
    private String experienceLevelLabel;
    private OffsetDateTime createdAt;
}
