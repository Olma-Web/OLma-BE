package com.olma.service;

import com.olma.domain.entity.BaseRate;
import com.olma.domain.entity.ExperienceLevel;
import com.olma.domain.entity.JobCategory;
import com.olma.domain.entity.SavedEstimate;
import com.olma.domain.entity.User;
import com.olma.domain.enums.EstimateAddon;
import com.olma.domain.repository.BaseRateRepository;
import com.olma.domain.repository.ExperienceLevelRepository;
import com.olma.domain.repository.JobCategoryRepository;
import com.olma.domain.repository.SavedEstimateRepository;
import com.olma.domain.repository.UserRepository;
import com.olma.dto.EstimateCalculateRequest;
import com.olma.dto.EstimateCalculateResponse;
import com.olma.dto.SavedEstimateResponse;
import com.olma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstimateService {

    private final BaseRateRepository baseRateRepository;
    private final ExperienceLevelRepository experienceLevelRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final UserRepository userRepository;
    private final SavedEstimateRepository savedEstimateRepository;

    @Transactional(readOnly = true)
    public EstimateCalculateResponse calculate(EstimateCalculateRequest request) {
        return doCalculate(request, null);
    }

    @Transactional
    public EstimateCalculateResponse calculateAndSave(Long userId, EstimateCalculateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        return doCalculate(request, user);
    }

    private EstimateCalculateResponse doCalculate(EstimateCalculateRequest request, User user) {
        ExperienceLevel experienceLevel = experienceLevelRepository.findById(request.getExperienceLevelId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid experience level"));
        JobCategory jobCategory = jobCategoryRepository.findById(request.getJobCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid job category"));
        BaseRate baseRate = baseRateRepository
                .findByExperienceLevel_IdAndJobCategory_Id(request.getExperienceLevelId(), request.getJobCategoryId())
                .orElseThrow(() -> new NotFoundException("Base rate not found for given spec"));

        int baseDailyRate = baseRate.getAmount();

        // Step 1: 기본 작업비
        int step1 = baseDailyRate * request.getScreenCount();

        // Step 2: UX 기획 관여도 multiplier
        BigDecimal uxMultiplier = request.getUxEngagement().getMultiplier();
        int step2 = new BigDecimal(step1)
                .multiply(uxMultiplier)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        // Step 3: 플랫폼 환경 multiplier
        BigDecimal platformMultiplier = request.getPlatformEnvironment().getMultiplier();
        int step3 = new BigDecimal(step2)
                .multiply(platformMultiplier)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        // Step 4: 산출물 양도 및 부가 서비스
        List<EstimateAddon> addons = request.getAddons() != null ? request.getAddons() : List.of();
        int addonPercent = addons.stream().mapToInt(EstimateAddon::getPercent).sum();
        int step4Addition = new BigDecimal(step3)
                .multiply(new BigDecimal(addonPercent))
                .divide(new BigDecimal(100), 0, RoundingMode.HALF_UP)
                .intValue();
        int finalAmount = step3 + step4Addition;

        List<String> addonNames = addons.stream().map(EstimateAddon::name).toList();

        Long savedEstimateId = null;
        SavedEstimate saved = null;
        if (user != null) {
            saved = savedEstimateRepository.save(SavedEstimate.builder()
                    .user(user)
                    .experienceLevel(experienceLevel)
                    .jobCategory(jobCategory)
                    .baseAmount(baseDailyRate)
                    .screenCount(request.getScreenCount())
                    .uxMultiplier(uxMultiplier)
                    .platformMultiplier(platformMultiplier)
                    .addons(addonNames)
                    .addonPercent(addonPercent)
                    .finalAmount(finalAmount)
                    .build());
            savedEstimateId = saved.getId();
        }

        return EstimateCalculateResponse.builder()
                .savedEstimateId(savedEstimateId)
                .experienceLevelLabel(experienceLevel.getLabel())
                .jobCategoryName(jobCategory.getName())
                .baseDailyRate(baseDailyRate)
                .screenCount(request.getScreenCount())
                .step1BasicFee(step1)
                .uxMultiplier(uxMultiplier)
                .step2UxFee(step2)
                .platformMultiplier(platformMultiplier)
                .step3PlatformFee(step3)
                .addons(addonNames)
                .addonPercent(addonPercent)
                .step4AddonFee(step4Addition)
                .finalAmount(finalAmount)
                .createdAt(saved != null ? saved.getCreatedAt() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SavedEstimateResponse> getMyEstimates(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: id=" + userId);
        }
        return savedEstimateRepository.findAllByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toSavedResponse)
                .toList();
    }

    @Transactional
    public void deleteEstimate(Long userId, Long estimateId) {
        SavedEstimate estimate = savedEstimateRepository.findById(estimateId)
                .orElseThrow(() -> new NotFoundException("Estimate not found: id=" + estimateId));
        if (!estimate.getUser().getId().equals(userId)) {
            throw new NotFoundException("Estimate not found: id=" + estimateId);
        }
        savedEstimateRepository.delete(estimate);
    }

    private SavedEstimateResponse toSavedResponse(SavedEstimate e) {
        return SavedEstimateResponse.builder()
                .id(e.getId())
                .experienceLevelLabel(e.getExperienceLevel().getLabel())
                .jobCategoryName(e.getJobCategory().getName())
                .screenCount(e.getScreenCount())
                .uxMultiplier(e.getUxMultiplier())
                .platformMultiplier(e.getPlatformMultiplier())
                .addons(e.getAddons())
                .addonPercent(e.getAddonPercent())
                .finalAmount(e.getFinalAmount())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
