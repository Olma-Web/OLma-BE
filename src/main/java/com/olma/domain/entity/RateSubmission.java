package com.olma.domain.entity;

import com.olma.domain.enums.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "rate_submissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RateSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_category_id", nullable = false)
    private JobCategory jobCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_type_id", nullable = false)
    private WorkType workType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experience_level_id", nullable = false)
    private ExperienceLevel experienceLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SubmissionType submissionType;

    @Column(nullable = false)
    private Boolean isRemote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Complexity complexity = Complexity.MEDIUM;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal durationMonths;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AmountUnit amountUnit;

    private Integer normalizedMonthly;

    @Column(nullable = false)
    private Boolean isSeed = false;

    @Column(nullable = false)
    private Boolean isOutlier = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SubmissionStatus status = SubmissionStatus.ACTIVE;

    @Column(nullable = false)
    private UUID sessionId;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder
    public RateSubmission(JobCategory jobCategory, WorkType workType, Region region,
                          ExperienceLevel experienceLevel, SubmissionType submissionType,
                          Boolean isRemote, Complexity complexity, BigDecimal durationMonths,
                          Integer amount, AmountUnit amountUnit, UUID sessionId) {
        this.jobCategory = jobCategory;
        this.workType = workType;
        this.region = region;
        this.experienceLevel = experienceLevel;
        this.submissionType = submissionType;
        this.isRemote = isRemote;
        this.complexity = complexity != null ? complexity : Complexity.MEDIUM;
        this.durationMonths = durationMonths;
        this.amount = amount;
        this.amountUnit = amountUnit;
        this.sessionId = sessionId;
        this.normalizedMonthly = calculateNormalizedMonthly();
    }

    private Integer calculateNormalizedMonthly() {
        if (amountUnit == AmountUnit.MONTHLY) {
            return amount;
        }
        if (durationMonths != null && durationMonths.compareTo(BigDecimal.ZERO) > 0) {
            return BigDecimal.valueOf(amount)
                    .divide(durationMonths, 0, java.math.RoundingMode.HALF_UP)
                    .intValue();
        }
        return amount;
    }
}
