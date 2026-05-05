package com.olma.domain.entity;

import com.olma.domain.enums.AmountUnit;
import com.olma.domain.enums.SubmissionStatus;
import com.olma.domain.enums.SubmissionType;
import com.olma.domain.enums.WorkFormat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @JoinColumn(name = "experience_level_id", nullable = false)
    private ExperienceLevel experienceLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SubmissionType submissionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_format", nullable = false, length = 10)
    private WorkFormat workFormat;

    @Column(nullable = false, length = 50)
    private String duration;

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
    public RateSubmission(JobCategory jobCategory, ExperienceLevel experienceLevel, User user,
                          SubmissionType submissionType, WorkFormat workFormat, String duration,
                          Integer amount, AmountUnit amountUnit, UUID sessionId) {
        this.jobCategory = jobCategory;
        this.experienceLevel = experienceLevel;
        this.user = user;
        this.submissionType = submissionType;
        this.workFormat = workFormat;
        this.duration = duration;
        this.amount = amount;
        this.amountUnit = amountUnit;
        this.sessionId = sessionId;
        this.normalizedMonthly = calculateNormalizedMonthly();
    }

    private Integer calculateNormalizedMonthly() {
        if (amountUnit == AmountUnit.MONTHLY) {
            return amount;
        }
        BigDecimal months = parseDurationToMonths(duration);
        if (months != null && months.compareTo(BigDecimal.ZERO) > 0) {
            return BigDecimal.valueOf(amount)
                    .divide(months, 0, RoundingMode.HALF_UP)
                    .intValue();
        }
        return null;
    }

    private static BigDecimal parseDurationToMonths(String d) {
        if (d == null || d.isBlank()) {
            return null;
        }
        return switch (d) {
            case "1주일 이하" -> new BigDecimal("0.25");
            case "2~3주" -> new BigDecimal("0.625");
            case "1개월" -> BigDecimal.ONE;
            case "2~3개월" -> new BigDecimal("2.5");
            case "3개월 이상" -> new BigDecimal("3");
            default -> tryParseNumeric(d);
        };
    }

    private static BigDecimal tryParseNumeric(String d) {
        try {
            return new BigDecimal(d);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
