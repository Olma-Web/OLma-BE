package com.olma.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.olma.domain.enums.NegotiationSimulationStatus;
import com.olma.domain.enums.PlatformEnvironment;
import com.olma.domain.enums.UxEngagement;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.olma.domain.value.EstimateNegotiationResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "saved_estimates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedEstimate {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter PROJECT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yy.MM.dd");

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

    @Enumerated(EnumType.STRING)
    @Column(name = "ux_engagement", nullable = false, length = 20)
    private UxEngagement uxEngagement;

    @Column(name = "ux_multiplier", nullable = false, precision = 3, scale = 2)
    private BigDecimal uxMultiplier;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform_environment", nullable = false, length = 20)
    private PlatformEnvironment platformEnvironment;

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

    @Column(name = "project_name", nullable = false, length = 100)
    private String projectName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "negotiation_result", columnDefinition = "jsonb")
    private EstimateNegotiationResult negotiationResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "negotiation_simulation_status", nullable = false, length = 20)
    private NegotiationSimulationStatus negotiationSimulationStatus = NegotiationSimulationStatus.NOT_STARTED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "negotiation_simulation_state", nullable = false, columnDefinition = "jsonb")
    private JsonNode negotiationSimulationState = JsonNodeFactory.instance.objectNode();

    @Column(name = "negotiation_simulation_started_at")
    private OffsetDateTime negotiationSimulationStartedAt;

    @Column(name = "negotiation_simulation_updated_at")
    private OffsetDateTime negotiationSimulationUpdatedAt;

    @Column(name = "negotiation_simulation_completed_at")
    private OffsetDateTime negotiationSimulationCompletedAt;

    @Builder
    public SavedEstimate(User user, ExperienceLevel experienceLevel, JobCategory jobCategory,
                         Integer baseAmount, Integer screenCount,
                         UxEngagement uxEngagement, BigDecimal uxMultiplier,
                         PlatformEnvironment platformEnvironment, BigDecimal platformMultiplier,
                         List<String> addons, Integer addonPercent, Integer finalAmount,
                         String projectName, EstimateNegotiationResult negotiationResult) {
        this.user = user;
        this.experienceLevel = experienceLevel;
        this.jobCategory = jobCategory;
        this.baseAmount = baseAmount;
        this.screenCount = screenCount;
        this.uxEngagement = uxEngagement;
        this.uxMultiplier = uxMultiplier;
        this.platformEnvironment = platformEnvironment;
        this.platformMultiplier = platformMultiplier;
        this.addons = addons != null ? addons : new ArrayList<>();
        this.addonPercent = addonPercent != null ? addonPercent : 0;
        this.finalAmount = finalAmount;
        this.projectName = normalizeProjectName(projectName);
        this.negotiationResult = negotiationResult;
    }

    public void updateProjectName(String projectName) {
        this.projectName = normalizeProjectName(projectName);
    }

    public void mergeSaveRequest(String projectName, EstimateNegotiationResult negotiationResult) {
        if (projectName != null && !projectName.isBlank()) {
            updateProjectName(projectName);
        }
        if (negotiationResult != null) {
            this.negotiationResult = negotiationResult;
        }
    }

    public void markNegotiationSimulationStarted() {
        if (negotiationSimulationStatus == NegotiationSimulationStatus.NOT_STARTED) {
            OffsetDateTime now = OffsetDateTime.now();
            negotiationSimulationStatus = NegotiationSimulationStatus.IN_PROGRESS;
            negotiationSimulationStartedAt = now;
            negotiationSimulationUpdatedAt = now;
        }
    }

    public void updateNegotiationSimulationProgress(JsonNode state) {
        if (negotiationSimulationStatus == NegotiationSimulationStatus.COMPLETED) {
            throw new IllegalArgumentException("Negotiation simulation is already completed");
        }
        markNegotiationSimulationStarted();
        negotiationSimulationState = state != null ? state : JsonNodeFactory.instance.objectNode();
        negotiationSimulationUpdatedAt = OffsetDateTime.now();
    }

    public void completeNegotiationSimulation(JsonNode state) {
        if (negotiationSimulationStatus == NegotiationSimulationStatus.COMPLETED) {
            return;
        }
        markNegotiationSimulationStarted();
        if (state != null) {
            negotiationSimulationState = state;
        }
        OffsetDateTime now = OffsetDateTime.now();
        negotiationSimulationStatus = NegotiationSimulationStatus.COMPLETED;
        negotiationSimulationUpdatedAt = now;
        negotiationSimulationCompletedAt = now;
    }

    private String normalizeProjectName(String value) {
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        return createdAt.atZoneSameInstant(KST).format(PROJECT_DATE_FORMATTER) + " 견적서";
    }
}
