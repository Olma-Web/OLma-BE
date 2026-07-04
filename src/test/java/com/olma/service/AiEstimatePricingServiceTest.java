package com.olma.service;

import com.olma.dto.AiEstimateAnalysis;
import com.olma.dto.AiEstimateCalculateRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiEstimatePricingServiceTest {

    private final AiEstimatePricingService pricingService = new AiEstimatePricingService();

    @Test
    void monteCarloPercentilesAreOrdered() {
        AiEstimatePricingService.PricingResult result = pricingService.calculate(baseRequest(), baseAnalysis());

        assertThat(result.getSchedule().getP50Days()).isLessThanOrEqualTo(result.getSchedule().getP80Days());
        assertThat(result.getSchedule().getP80Days()).isLessThanOrEqualTo(result.getSchedule().getP95Days());
        assertThat(result.getSchedule().getSimulationCount()).isEqualTo(10_000);
    }

    @Test
    void detectsBudgetAndTimelineRisks() {
        AiEstimateCalculateRequest request = baseRequest();
        request.setDesiredTimelineDays(3);
        request.setBudgetAmount(1_000_000);

        AiEstimatePricingService.PricingResult result = pricingService.calculate(request, baseAnalysis());

        assertThat(result.getRisks()).anyMatch(r -> "희망 일정 과소".equals(r.getTitle()));
        assertThat(result.getRisks()).anyMatch(r -> "예산 부족".equals(r.getTitle()));
    }

    @Test
    void calculatesFeatureAmountsAndBreakdown() {
        AiEstimatePricingService.PricingResult result = pricingService.calculate(baseRequest(), baseAnalysis());

        assertThat(result.getFinalAmount()).isPositive();
        assertThat(result.getFeatures()).allSatisfy(feature -> assertThat(feature.getAmount()).isPositive());
        assertThat(result.getBreakdown()).extracting("category")
                .containsExactly("기획/UX", "UI 디자인", "QA/검수", "핸드오프");
    }

    private AiEstimateCalculateRequest baseRequest() {
        AiEstimateCalculateRequest request = new AiEstimateCalculateRequest();
        request.setProjectDescription("반응형 웹 기반 쇼핑몰 리디자인이 필요합니다. 로그인, 상품 상세, 장바구니, 결제, 마이페이지 화면을 포함합니다.");
        request.setPlatformHint("반응형 웹");
        return request;
    }

    private AiEstimateAnalysis baseAnalysis() {
        AiEstimateAnalysis analysis = new AiEstimateAnalysis();
        analysis.projectTitle = "쇼핑몰 리디자인";
        analysis.platform = "반응형 웹";
        analysis.estimatedScreenCount = 18;
        analysis.clientMessage = "반응형 쇼핑몰의 핵심 구매 여정을 기준으로 산정했습니다.";
        analysis.features = List.of(
                seed("정보구조/UX 플로우", "구매 여정과 IA 설계", "HIGH", 3, 5, 8, "UX", "와이어프레임"),
                seed("상품/장바구니 UI", "상품 탐색과 장바구니 화면 디자인", "MEDIUM", 4, 6, 9, "UI", "UI 시안"),
                seed("결제 플로우 검수", "결제 예외 상태와 사용성 검수", "HIGH", 2, 4, 7, "QA", "QA 체크리스트")
        );
        return analysis;
    }

    private AiEstimateAnalysis.AiEstimateFeatureSeed seed(String name, String description, String complexity,
                                                         int minDays, int expectedDays, int maxDays,
                                                         String role, String deliverable) {
        AiEstimateAnalysis.AiEstimateFeatureSeed seed = new AiEstimateAnalysis.AiEstimateFeatureSeed();
        seed.name = name;
        seed.description = description;
        seed.complexity = complexity;
        seed.minDays = minDays;
        seed.expectedDays = expectedDays;
        seed.maxDays = maxDays;
        seed.role = role;
        seed.deliverable = deliverable;
        return seed;
    }
}
