package com.olma.service;

import com.olma.domain.value.AiEstimateBreakdown;
import com.olma.domain.value.AiEstimateFeature;
import com.olma.domain.value.AiEstimateRisk;
import com.olma.domain.value.AiEstimateSchedule;
import com.olma.dto.AiEstimateAnalysis;
import com.olma.dto.AiEstimateCalculateRequest;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
public class AiEstimatePricingService {

    private static final int BASE_DAILY_RATE = 420_000;
    private static final int SIMULATION_COUNT = 10_000;

    public PricingResult calculate(AiEstimateCalculateRequest request, AiEstimateAnalysis analysis) {
        List<AiEstimateFeature> features = normalizeFeatures(analysis);
        int expectedDays = features.stream().mapToInt(AiEstimateFeature::getExpectedDays).sum();
        double platformMultiplier = platformMultiplier(analysis.platform);
        int featureAmount = features.stream().mapToInt(AiEstimateFeature::getAmount).sum();
        int finalAmount = roundToTenThousand((int) Math.round(featureAmount * platformMultiplier));
        AiEstimateSchedule schedule = simulateSchedule(features, request.getProjectDescription());
        List<AiEstimateRisk> risks = detectRisks(request, analysis, features, schedule, finalAmount);
        List<AiEstimateBreakdown> breakdown = buildBreakdown(features, finalAmount);

        return PricingResult.builder()
                .features(features)
                .totalExpectedDays(expectedDays)
                .finalAmount(finalAmount)
                .schedule(schedule)
                .risks(risks)
                .breakdown(breakdown)
                .clientMessage(normalizeClientMessage(analysis, finalAmount, schedule))
                .build();
    }

    private List<AiEstimateFeature> normalizeFeatures(AiEstimateAnalysis analysis) {
        List<AiEstimateAnalysis.AiEstimateFeatureSeed> seeds =
                analysis.features != null ? analysis.features : List.of();
        if (seeds.isEmpty()) {
            throw new IllegalArgumentException("AI가 기능 분해 결과를 생성하지 못했습니다.");
        }

        List<AiEstimateFeature> features = new ArrayList<>();
        for (AiEstimateAnalysis.AiEstimateFeatureSeed seed : seeds) {
            int minDays = clamp(seed.minDays, 1, 60);
            int expectedDays = clamp(seed.expectedDays, minDays, 90);
            int maxDays = clamp(seed.maxDays, expectedDays, 120);
            String complexity = normalizeComplexity(seed.complexity);
            int amount = roundToTenThousand((int) Math.round(expectedDays * BASE_DAILY_RATE * complexityMultiplier(complexity)));
            features.add(AiEstimateFeature.builder()
                    .name(nonBlank(seed.name, "작업 항목"))
                    .description(nonBlank(seed.description, "요구사항 기반 UI/UX 작업입니다."))
                    .complexity(complexity)
                    .minDays(minDays)
                    .expectedDays(expectedDays)
                    .maxDays(maxDays)
                    .role(normalizeRole(seed.role))
                    .deliverable(nonBlank(seed.deliverable, "디자인 산출물"))
                    .amount(amount)
                    .build());
        }
        return features;
    }

    private AiEstimateSchedule simulateSchedule(List<AiEstimateFeature> features, String seedText) {
        List<Integer> totals = new ArrayList<>(SIMULATION_COUNT);
        Random random = new Random(seedText != null ? seedText.hashCode() : 42);
        for (int i = 0; i < SIMULATION_COUNT; i++) {
            int total = 0;
            for (AiEstimateFeature feature : features) {
                total += triangular(random, feature.getMinDays(), feature.getExpectedDays(), feature.getMaxDays());
            }
            totals.add(total);
        }
        totals.sort(Comparator.naturalOrder());
        return AiEstimateSchedule.builder()
                .p50Days(percentile(totals, 0.50))
                .p80Days(percentile(totals, 0.80))
                .p95Days(percentile(totals, 0.95))
                .simulationCount(SIMULATION_COUNT)
                .build();
    }

    private List<AiEstimateRisk> detectRisks(AiEstimateCalculateRequest request, AiEstimateAnalysis analysis,
                                             List<AiEstimateFeature> features, AiEstimateSchedule schedule,
                                             int finalAmount) {
        List<AiEstimateRisk> risks = new ArrayList<>();
        if (request.getDesiredTimelineDays() != null && request.getDesiredTimelineDays() < schedule.getP80Days()) {
            risks.add(AiEstimateRisk.builder()
                    .level("HIGH")
                    .title("희망 일정 과소")
                    .message("80% 신뢰 일정(" + schedule.getP80Days() + "일)보다 희망 일정이 짧아 범위 조정이나 단계적 오픈이 필요합니다.")
                    .build());
        }
        if (request.getBudgetAmount() != null && request.getBudgetAmount() < finalAmount) {
            risks.add(AiEstimateRisk.builder()
                    .level("HIGH")
                    .title("예산 부족")
                    .message("입력 예산이 권장 견적보다 낮아 핵심 화면 우선순위 조정이 필요합니다.")
                    .build());
        }
        if (features.size() >= 8 || safeInt(analysis.estimatedScreenCount) >= 20) {
            risks.add(AiEstimateRisk.builder()
                    .level("MEDIUM")
                    .title("범위 확장 가능성")
                    .message("기능 또는 화면 수가 많아 상태 화면, 예외 플로우, 수정 라운드가 누락될 수 있습니다.")
                    .build());
        }
        boolean hasHighComplexity = features.stream().anyMatch(f -> "HIGH".equals(f.getComplexity()));
        if (hasHighComplexity) {
            risks.add(AiEstimateRisk.builder()
                    .level("MEDIUM")
                    .title("고복잡도 플로우 포함")
                    .message("복잡도가 높은 플로우는 와이어프레임 검증과 프로토타입 리뷰 일정을 별도로 확보하는 것이 좋습니다.")
                    .build());
        }
        if (risks.isEmpty()) {
            risks.add(AiEstimateRisk.builder()
                    .level("LOW")
                    .title("주요 리스크 낮음")
                    .message("현재 입력 기준으로 일정과 범위의 주요 충돌은 크지 않습니다.")
                    .build());
        }
        return risks;
    }

    private List<AiEstimateBreakdown> buildBreakdown(List<AiEstimateFeature> features, int finalAmount) {
        int planning = amountByRole(features, "PLANNING", "UX");
        int ui = amountByRole(features, "UI");
        int qa = amountByRole(features, "QA");
        int handoff = amountByRole(features, "HANDOFF");
        int remaining = Math.max(0, finalAmount - planning - ui - qa - handoff);
        if (remaining > 0) {
            ui += remaining;
        }
        return List.of(
                breakdown("기획/UX", planning, finalAmount),
                breakdown("UI 디자인", ui, finalAmount),
                breakdown("QA/검수", qa, finalAmount),
                breakdown("핸드오프", handoff, finalAmount)
        );
    }

    private String normalizeClientMessage(AiEstimateAnalysis analysis, int finalAmount, AiEstimateSchedule schedule) {
        if (analysis.clientMessage != null && !analysis.clientMessage.isBlank()) {
            return analysis.clientMessage.trim();
        }
        return "본 견적은 요구사항을 UI/UX 화면 설계와 디자인 산출물 기준으로 분해해 산정했으며, 권장 견적은 "
                + finalAmount + "원, 80% 신뢰 일정은 " + schedule.getP80Days() + "일입니다.";
    }

    private int amountByRole(List<AiEstimateFeature> features, String... roles) {
        int sum = 0;
        for (AiEstimateFeature feature : features) {
            for (String role : roles) {
                if (role.equals(feature.getRole())) {
                    sum += feature.getAmount();
                    break;
                }
            }
        }
        return sum;
    }

    private AiEstimateBreakdown breakdown(String category, int amount, int total) {
        return AiEstimateBreakdown.builder()
                .category(category)
                .amount(amount)
                .ratio(total > 0 ? (int) Math.round(amount * 100.0 / total) : 0)
                .build();
    }

    private int triangular(Random random, int min, int mode, int max) {
        double u = random.nextDouble();
        double c = (double) (mode - min) / (max - min + 1);
        double value = u < c
                ? min + Math.sqrt(u * (max - min) * (mode - min))
                : max - Math.sqrt((1 - u) * (max - min) * (max - mode));
        return Math.max(1, (int) Math.round(value));
    }

    private int percentile(List<Integer> sorted, double percentile) {
        int index = Math.min(sorted.size() - 1, Math.max(0, (int) Math.ceil(sorted.size() * percentile) - 1));
        return sorted.get(index);
    }

    private double platformMultiplier(String platform) {
        String normalized = platform != null ? platform.toLowerCase(Locale.ROOT) : "";
        if (normalized.contains("반응형")) return 1.15;
        if (normalized.contains("앱") || normalized.contains("ios") || normalized.contains("android")) return 1.10;
        if (normalized.contains("관리자")) return 1.05;
        return 1.0;
    }

    private String normalizeComplexity(String value) {
        if (value == null) return "MEDIUM";
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "LOW", "MEDIUM", "HIGH" -> normalized;
            default -> "MEDIUM";
        };
    }

    private double complexityMultiplier(String complexity) {
        return switch (complexity) {
            case "LOW" -> 1.0;
            case "HIGH" -> 1.55;
            default -> 1.25;
        };
    }

    private String normalizeRole(String value) {
        if (value == null) return "UI";
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "PLANNING", "UX", "UI", "QA", "HANDOFF" -> normalized;
            default -> "UI";
        };
    }

    private int roundToTenThousand(int value) {
        return (int) Math.round(value / 10_000.0) * 10_000;
    }

    private int clamp(Integer value, int min, int max) {
        int actual = value != null ? value : min;
        return Math.min(max, Math.max(min, actual));
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    private String nonBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }

    @Getter
    @Builder
    public static class PricingResult {
        private List<AiEstimateFeature> features;
        private Integer totalExpectedDays;
        private Integer finalAmount;
        private AiEstimateSchedule schedule;
        private List<AiEstimateRisk> risks;
        private List<AiEstimateBreakdown> breakdown;
        private String clientMessage;
    }
}
