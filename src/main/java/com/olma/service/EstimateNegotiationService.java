package com.olma.service;

import com.olma.domain.entity.BaseRate;
import com.olma.domain.enums.EstimateAddon;
import com.olma.domain.enums.PlatformEnvironment;
import com.olma.domain.enums.UxEngagement;
import com.olma.domain.repository.BaseRateRepository;
import com.olma.domain.value.EstimateNegotiationOption;
import com.olma.domain.value.EstimateNegotiationResult;
import com.olma.dto.EstimateCalculateRequest;
import com.olma.dto.EstimateNegotiationRequest;
import com.olma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstimateNegotiationService {

    private final BaseRateRepository baseRateRepository;

    @Transactional(readOnly = true)
    public EstimateNegotiationResult simulate(EstimateNegotiationRequest request) {
        BaseRate baseRate = baseRateRepository
                .findByExperienceLevel_IdAndJobCategory_Id(request.getExperienceLevelId(), request.getJobCategoryId())
                .orElseThrow(() -> new NotFoundException("Base rate not found for given spec"));
        return simulate(request, request.getTargetBudgetAmount(), baseRate.getAmount());
    }

    public EstimateNegotiationResult simulate(EstimateCalculateRequest request, Integer targetBudgetAmount, int baseAmount) {
        if (targetBudgetAmount == null) {
            return null;
        }
        int currentAmount = calculateAmount(
                baseAmount,
                request.getScreenCount(),
                request.getUxEngagement(),
                request.getPlatformEnvironment(),
                request.getAddons());

        if (targetBudgetAmount >= currentAmount) {
            return EstimateNegotiationResult.builder()
                    .status("NO_DISCOUNT_NEEDED")
                    .currentAmount(currentAmount)
                    .targetBudgetAmount(targetBudgetAmount)
                    .gapAmount(0)
                    .recommendedOptionType(null)
                    .options(List.of())
                    .clientMessage("입력한 예산이 현재 견적 이상이므로 범위를 줄이지 않아도 진행 가능한 조건입니다.")
                    .build();
        }

        List<EstimateNegotiationOption> options = List.of(
                scopeReductionOption(request, baseAmount, currentAmount, targetBudgetAmount),
                addonRemovalOption(request, baseAmount, currentAmount, targetBudgetAmount),
                leanDeliveryOption(request, baseAmount, currentAmount, targetBudgetAmount)
        );

        EstimateNegotiationOption recommended = options.stream()
                .filter(option -> option.getAdjustedAmount() <= targetBudgetAmount)
                .max(Comparator.comparingInt(EstimateNegotiationOption::getAdjustedAmount))
                .orElseGet(() -> options.stream()
                        .min(Comparator.comparingInt(EstimateNegotiationOption::getGapAfterAdjustment))
                        .orElse(options.get(0)));

        int gapAmount = currentAmount - targetBudgetAmount;
        return EstimateNegotiationResult.builder()
                .status("ADJUSTMENT_REQUIRED")
                .currentAmount(currentAmount)
                .targetBudgetAmount(targetBudgetAmount)
                .gapAmount(gapAmount)
                .recommendedOptionType(recommended.getType())
                .options(options)
                .clientMessage("현재 견적은 예산보다 " + toManText(gapAmount) + " 높습니다. 가격만 낮추기보다 작업 범위를 함께 조정하는 조건으로 제안하는 것을 권장합니다.")
                .build();
    }

    private EstimateNegotiationOption scopeReductionOption(EstimateCalculateRequest request, int baseAmount,
                                                           int currentAmount, int targetBudgetAmount) {
        int adjustedScreenCount = Math.max(1, (int) Math.floor(request.getScreenCount() * 0.75));
        if (request.getScreenCount() > 1 && adjustedScreenCount == request.getScreenCount()) {
            adjustedScreenCount = request.getScreenCount() - 1;
        }
        int adjustedAmount = calculateAmount(
                baseAmount,
                adjustedScreenCount,
                request.getUxEngagement(),
                request.getPlatformEnvironment(),
                request.getAddons());
        int reducedScreens = request.getScreenCount() - adjustedScreenCount;
        return option(
                "SCOPE_REDUCTION",
                "화면 범위 축소안",
                adjustedAmount,
                currentAmount,
                targetBudgetAmount,
                adjustedScreenCount,
                request.getUxEngagement(),
                request.getAddons(),
                List.of(
                        "핵심 화면 우선 진행",
                        "화면 수 " + request.getScreenCount() + "개 -> " + adjustedScreenCount + "개",
                        "후순위 " + reducedScreens + "개 화면은 2차 범위로 분리"
                ),
                "예산에 맞추기 위해 핵심 화면을 먼저 진행하고, 나머지 화면은 2차 작업으로 분리하는 조건을 제안드립니다."
        );
    }

    private EstimateNegotiationOption addonRemovalOption(EstimateCalculateRequest request, int baseAmount,
                                                         int currentAmount, int targetBudgetAmount) {
        int adjustedAmount = calculateAmount(
                baseAmount,
                request.getScreenCount(),
                request.getUxEngagement(),
                request.getPlatformEnvironment(),
                List.of());
        List<String> adjustments = request.getAddons() == null || request.getAddons().isEmpty()
                ? List.of("제외 가능한 부가 산출물이 없어 금액 절감 폭이 제한적입니다.")
                : List.of("부가 산출물 제외", "프로토타입/디자인 시스템/원본 양도는 별도 견적으로 분리");
        return option(
                "ADDON_REMOVAL",
                "부가 산출물 제외안",
                adjustedAmount,
                currentAmount,
                targetBudgetAmount,
                request.getScreenCount(),
                request.getUxEngagement(),
                List.of(),
                adjustments,
                "예산에 맞춰 진행하되, 부가 산출물은 제외하고 화면 디자인 중심으로 범위를 조정하는 조건을 제안드립니다."
        );
    }

    private EstimateNegotiationOption leanDeliveryOption(EstimateCalculateRequest request, int baseAmount,
                                                         int currentAmount, int targetBudgetAmount) {
        int adjustedScreenCount = Math.max(1, (int) Math.floor(request.getScreenCount() * 0.65));
        UxEngagement adjustedUx = UxEngagement.GUI_ONLY;
        int adjustedAmount = calculateAmount(
                baseAmount,
                adjustedScreenCount,
                adjustedUx,
                request.getPlatformEnvironment(),
                List.of());
        return option(
                "LEAN_DELIVERY",
                "최소 진행안",
                adjustedAmount,
                currentAmount,
                targetBudgetAmount,
                adjustedScreenCount,
                adjustedUx,
                List.of(),
                List.of(
                        "GUI 중심으로 진행",
                        "화면 수 " + request.getScreenCount() + "개 -> " + adjustedScreenCount + "개",
                        "부가 산출물 제외",
                        "UX 기획/검증 범위 최소화"
                ),
                "해당 예산에서는 GUI 중심의 최소 범위로 진행하고, UX 기획과 부가 산출물은 후속 작업으로 분리하는 조건을 권장드립니다."
        );
    }

    private EstimateNegotiationOption option(String type, String title, int adjustedAmount, int currentAmount,
                                             int targetBudgetAmount, int adjustedScreenCount,
                                             UxEngagement uxEngagement, List<EstimateAddon> addons,
                                             List<String> adjustments, String clientMessage) {
        int savingAmount = Math.max(0, currentAmount - adjustedAmount);
        int gapAfterAdjustment = Math.max(0, adjustedAmount - targetBudgetAmount);
        return EstimateNegotiationOption.builder()
                .type(type)
                .title(title)
                .adjustedAmount(adjustedAmount)
                .savingAmount(savingAmount)
                .gapAfterAdjustment(gapAfterAdjustment)
                .adjustedScreenCount(adjustedScreenCount)
                .uxEngagement(uxEngagement.name())
                .addons(addons != null ? addons.stream().map(EstimateAddon::name).toList() : List.of())
                .adjustments(adjustments)
                .clientMessage(clientMessage)
                .build();
    }

    private int calculateAmount(int baseAmount, int screenCount, UxEngagement uxEngagement,
                                PlatformEnvironment platformEnvironment, List<EstimateAddon> addons) {
        int step1 = baseAmount * screenCount;
        int step2 = BigDecimal.valueOf(step1)
                .multiply(uxEngagement.getMultiplier())
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        int step3 = BigDecimal.valueOf(step2)
                .multiply(platformEnvironment.getMultiplier())
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        int addonPercent = addons != null ? addons.stream().mapToInt(EstimateAddon::getPercent).sum() : 0;
        int addonAmount = BigDecimal.valueOf(step3)
                .multiply(BigDecimal.valueOf(addonPercent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .intValue();
        return step3 + addonAmount;
    }

    private String toManText(int amount) {
        return Math.round(amount / 10_000.0) + "만 원";
    }
}
