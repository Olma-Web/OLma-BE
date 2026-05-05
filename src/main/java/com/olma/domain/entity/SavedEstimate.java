package com.olma.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "saved_estimates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedEstimate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experience_level_id", nullable = false)
    private ExperienceLevel experienceLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_category_id", nullable = false)
    private JobCategory jobCategory;

    @Column(name = "base_amount", nullable = false)
    private Integer baseAmount;

    @Column(name = "screen_count", nullable = false)
    private Integer screenCount;

    @Column(name = "ux_multiplier", nullable = false, precision = 3, scale = 2)
    private BigDecimal uxMultiplier;

    @Column(name = "platform_multiplier", nullable = false, precision = 3, scale = 2)
    private BigDecimal platformMultiplier;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> addons = new ArrayList<>();

    @Column(name = "addon_percent", nullable = false)
    private Integer addonPercent = 0;

    @Column(name = "final_amount", nullable = false)
    private Integer finalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder
    public SavedEstimate(User user, ExperienceLevel experienceLevel, JobCategory jobCategory,
                         Integer baseAmount, Integer screenCount,
                         BigDecimal uxMultiplier, BigDecimal platformMultiplier,
                         List<String> addons, Integer addonPercent, Integer finalAmount) {
        this.user = user;
        this.experienceLevel = experienceLevel;
        this.jobCategory = jobCategory;
        this.baseAmount = baseAmount;
        this.screenCount = screenCount;
        this.uxMultiplier = uxMultiplier;
        this.platformMultiplier = platformMultiplier;
        this.addons = addons != null ? addons : new ArrayList<>();
        this.addonPercent = addonPercent != null ? addonPercent : 0;
        this.finalAmount = finalAmount;
    }
}
