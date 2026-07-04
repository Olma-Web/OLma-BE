package com.olma.domain.entity;

import com.olma.domain.value.AiEstimateBreakdown;
import com.olma.domain.value.AiEstimateFeature;
import com.olma.domain.value.AiEstimateRisk;
import com.olma.domain.value.AiEstimateSchedule;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "saved_ai_estimates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedAiEstimate {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter PROJECT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yy.MM.dd");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "project_name", nullable = false, length = 100)
    private String projectName;

    @Column(name = "project_description", nullable = false, length = 3000)
    private String projectDescription;

    @Column(nullable = false, length = 100)
    private String platform;

    @Column(name = "estimated_screen_count", nullable = false)
    private Integer estimatedScreenCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<AiEstimateFeature> features = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private AiEstimateSchedule schedule;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<AiEstimateRisk> risks = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<AiEstimateBreakdown> breakdown = new ArrayList<>();

    @Column(name = "total_expected_days", nullable = false)
    private Integer totalExpectedDays;

    @Column(name = "final_amount", nullable = false)
    private Integer finalAmount;

    @Column(name = "client_message", nullable = false, columnDefinition = "text")
    private String clientMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder
    public SavedAiEstimate(User user, String projectName, String projectDescription, String platform,
                           Integer estimatedScreenCount, List<AiEstimateFeature> features,
                           AiEstimateSchedule schedule, List<AiEstimateRisk> risks,
                           List<AiEstimateBreakdown> breakdown, Integer totalExpectedDays,
                           Integer finalAmount, String clientMessage) {
        this.user = user;
        this.projectName = normalizeProjectName(projectName);
        this.projectDescription = projectDescription;
        this.platform = platform;
        this.estimatedScreenCount = estimatedScreenCount;
        this.features = features != null ? features : new ArrayList<>();
        this.schedule = schedule;
        this.risks = risks != null ? risks : new ArrayList<>();
        this.breakdown = breakdown != null ? breakdown : new ArrayList<>();
        this.totalExpectedDays = totalExpectedDays;
        this.finalAmount = finalAmount;
        this.clientMessage = clientMessage;
    }

    private String normalizeProjectName(String value) {
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        return createdAt.atZoneSameInstant(KST).format(PROJECT_DATE_FORMATTER) + " AI 견적서";
    }
}
