package com.olma.service;

import com.olma.domain.enums.EstimateAddon;
import com.olma.domain.enums.PlatformEnvironment;
import com.olma.domain.enums.UxEngagement;
import com.olma.domain.repository.BaseRateRepository;
import com.olma.domain.value.EstimateNegotiationResult;
import com.olma.dto.EstimateCalculateRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EstimateNegotiationServiceTest {

    private final EstimateNegotiationService service = new EstimateNegotiationService(mock(BaseRateRepository.class));

    @Test
    void returnsNoDiscountNeededWhenBudgetIsEnough() {
        EstimateCalculateRequest request = request();

        EstimateNegotiationResult result = service.simulate(request, 10_000_000, 400_000);

        assertThat(result.getStatus()).isEqualTo("NO_DISCOUNT_NEEDED");
        assertThat(result.getGapAmount()).isZero();
        assertThat(result.getOptions()).isEmpty();
    }

    @Test
    void createsThreeOptionsWhenBudgetIsLower() {
        EstimateCalculateRequest request = request();

        EstimateNegotiationResult result = service.simulate(request, 2_000_000, 400_000);

        assertThat(result.getStatus()).isEqualTo("ADJUSTMENT_REQUIRED");
        assertThat(result.getOptions()).hasSize(3);
        assertThat(result.getRecommendedOptionType()).isNotBlank();
    }

    @Test
    void addonRemovalReducesAmountWhenAddonsExist() {
        EstimateCalculateRequest request = request();

        EstimateNegotiationResult result = service.simulate(request, 2_000_000, 400_000);

        assertThat(result.getOptions())
                .filteredOn(option -> "ADDON_REMOVAL".equals(option.getType()))
                .singleElement()
                .satisfies(option -> assertThat(option.getSavingAmount()).isPositive());
    }

    private EstimateCalculateRequest request() {
        EstimateCalculateRequest request = new EstimateCalculateRequest();
        request.setExperienceLevelId(1L);
        request.setJobCategoryId(14L);
        request.setScreenCount(8);
        request.setUxEngagement(UxEngagement.WIREFRAME_PLUS);
        request.setPlatformEnvironment(PlatformEnvironment.RESPONSIVE_WEB);
        request.setAddons(List.of(EstimateAddon.PROTOTYPING, EstimateAddon.DESIGN_SYSTEM));
        return request;
    }
}
