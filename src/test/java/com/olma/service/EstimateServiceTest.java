package com.olma.service;

import com.olma.domain.entity.BaseRate;
import com.olma.domain.entity.ExperienceLevel;
import com.olma.domain.entity.JobCategory;
import com.olma.domain.entity.SavedEstimate;
import com.olma.domain.entity.User;
import com.olma.domain.enums.EstimateAddon;
import com.olma.domain.enums.PlatformEnvironment;
import com.olma.domain.enums.UxEngagement;
import com.olma.domain.repository.BaseRateRepository;
import com.olma.domain.repository.ExperienceLevelRepository;
import com.olma.domain.repository.JobCategoryRepository;
import com.olma.domain.repository.SavedEstimateRepository;
import com.olma.domain.repository.UserRepository;
import com.olma.dto.EstimateCalculateRequest;
import com.olma.dto.EstimateCalculateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EstimateServiceTest {

    private final BaseRateRepository baseRateRepository = mock(BaseRateRepository.class);
    private final ExperienceLevelRepository experienceLevelRepository = mock(ExperienceLevelRepository.class);
    private final JobCategoryRepository jobCategoryRepository = mock(JobCategoryRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SavedEstimateRepository savedEstimateRepository = mock(SavedEstimateRepository.class);
    private final EstimateNegotiationService estimateNegotiationService =
            new EstimateNegotiationService(baseRateRepository);
    private final EstimateService service = new EstimateService(
            baseRateRepository,
            experienceLevelRepository,
            jobCategoryRepository,
            userRepository,
            savedEstimateRepository,
            estimateNegotiationService
    );

    private ExperienceLevel experienceLevel;
    private JobCategory jobCategory;
    private User user;

    @BeforeEach
    void setUp() {
        experienceLevel = newInstance(ExperienceLevel.class);
        ReflectionTestUtils.setField(experienceLevel, "id", 1L);
        ReflectionTestUtils.setField(experienceLevel, "label", "중급");

        jobCategory = newInstance(JobCategory.class);
        ReflectionTestUtils.setField(jobCategory, "id", 14L);
        ReflectionTestUtils.setField(jobCategory, "name", "UX/UI 디자인");

        user = User.builder()
                .email("user@example.com")
                .password("password")
                .nickname("tester")
                .build();
        ReflectionTestUtils.setField(user, "id", 7L);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(experienceLevelRepository.findById(1L)).thenReturn(Optional.of(experienceLevel));
        when(jobCategoryRepository.findById(14L)).thenReturn(Optional.of(jobCategory));
        when(baseRateRepository.findByExperienceLevel_IdAndJobCategory_Id(1L, 14L))
                .thenReturn(Optional.of(BaseRate.builder()
                        .experienceLevel(experienceLevel)
                        .jobCategory(jobCategory)
                        .amount(400_000)
                        .build()));
    }

    @Test
    void calculateAndSaveReusesSameEstimateAndMergesNegotiationResult() {
        EstimateCalculateRequest request = request();
        request.setNegotiationTargetBudgetAmount(2_000_000);
        SavedEstimate existing = SavedEstimate.builder()
                .user(user)
                .experienceLevel(experienceLevel)
                .jobCategory(jobCategory)
                .baseAmount(400_000)
                .screenCount(8)
                .uxEngagement(UxEngagement.WIREFRAME_PLUS)
                .uxMultiplier(UxEngagement.WIREFRAME_PLUS.getMultiplier())
                .platformEnvironment(PlatformEnvironment.RESPONSIVE_WEB)
                .platformMultiplier(PlatformEnvironment.RESPONSIVE_WEB.getMultiplier())
                .addons(List.of("PROTOTYPING", "DESIGN_SYSTEM"))
                .addonPercent(30)
                .finalAmount(8_112_000)
                .projectName("초기 견적서")
                .negotiationResult(null)
                .build();
        ReflectionTestUtils.setField(existing, "id", 55L);

        when(savedEstimateRepository.findAllByUser_IdAndExperienceLevel_IdAndJobCategory_IdAndScreenCountAndUxEngagementAndPlatformEnvironmentAndAddonPercentAndFinalAmountOrderByCreatedAtDesc(
                7L,
                1L,
                14L,
                8,
                UxEngagement.WIREFRAME_PLUS,
                PlatformEnvironment.RESPONSIVE_WEB,
                30,
                8_112_000
        )).thenReturn(List.of(existing));

        EstimateCalculateResponse response = service.calculateAndSave(7L, request);

        assertThat(response.getSavedEstimateId()).isEqualTo(55L);
        assertThat(response.getNegotiationResult()).isNotNull();
        assertThat(existing.getNegotiationResult()).isNotNull();
        verify(savedEstimateRepository, never()).save(any(SavedEstimate.class));
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

    private <T> T newInstance(Class<T> type) {
        try {
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot instantiate " + type.getSimpleName(), e);
        }
    }
}
