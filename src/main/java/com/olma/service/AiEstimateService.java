package com.olma.service;

import com.olma.domain.entity.SavedAiEstimate;
import com.olma.domain.entity.User;
import com.olma.domain.repository.SavedAiEstimateRepository;
import com.olma.domain.repository.UserRepository;
import com.olma.dto.AiEstimateAnalysis;
import com.olma.dto.AiEstimateCalculateRequest;
import com.olma.dto.AiEstimateResponse;
import com.olma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiEstimateService {

    private final AiEstimateAnalyzer analyzer;
    private final AiEstimatePricingService pricingService;
    private final UserRepository userRepository;
    private final SavedAiEstimateRepository savedAiEstimateRepository;

    @Transactional(readOnly = true)
    public AiEstimateResponse calculate(AiEstimateCalculateRequest request) {
        return doCalculate(request, null);
    }

    @Transactional
    public AiEstimateResponse calculateAndSave(Long userId, AiEstimateCalculateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        return doCalculate(request, user);
    }

    @Transactional(readOnly = true)
    public List<AiEstimateResponse> getMyAiEstimates(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: id=" + userId);
        }
        return savedAiEstimateRepository.findAllByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteAiEstimate(Long userId, Long estimateId) {
        SavedAiEstimate estimate = savedAiEstimateRepository.findById(estimateId)
                .orElseThrow(() -> new NotFoundException("AI estimate not found: id=" + estimateId));
        if (!estimate.getUser().getId().equals(userId)) {
            throw new NotFoundException("AI estimate not found: id=" + estimateId);
        }
        savedAiEstimateRepository.delete(estimate);
    }

    private AiEstimateResponse doCalculate(AiEstimateCalculateRequest request, User user) {
        AiEstimateAnalysis analysis = analyzer.analyze(request);
        validateAnalysis(analysis);
        AiEstimatePricingService.PricingResult pricing = pricingService.calculate(request, analysis);

        SavedAiEstimate saved = null;
        if (user != null) {
            saved = savedAiEstimateRepository.save(SavedAiEstimate.builder()
                    .user(user)
                    .projectName(request.getProjectName() != null ? request.getProjectName() : analysis.projectTitle)
                    .projectDescription(request.getProjectDescription().trim())
                    .platform(nonBlank(analysis.platform, "UI/UX"))
                    .estimatedScreenCount(Math.max(1, analysis.estimatedScreenCount))
                    .features(pricing.getFeatures())
                    .schedule(pricing.getSchedule())
                    .risks(pricing.getRisks())
                    .breakdown(pricing.getBreakdown())
                    .totalExpectedDays(pricing.getTotalExpectedDays())
                    .finalAmount(pricing.getFinalAmount())
                    .clientMessage(pricing.getClientMessage())
                    .build());
            log.info("ai estimate saved estimateId={} userId={}", saved.getId(), user.getId());
        }

        return AiEstimateResponse.builder()
                .savedEstimateId(saved != null ? saved.getId() : null)
                .projectName(saved != null ? saved.getProjectName() : nonBlank(analysis.projectTitle, null))
                .projectDescription(request.getProjectDescription().trim())
                .platform(nonBlank(analysis.platform, "UI/UX"))
                .estimatedScreenCount(Math.max(1, analysis.estimatedScreenCount))
                .features(pricing.getFeatures())
                .totalExpectedDays(pricing.getTotalExpectedDays())
                .finalAmount(pricing.getFinalAmount())
                .schedule(pricing.getSchedule())
                .risks(pricing.getRisks())
                .breakdown(pricing.getBreakdown())
                .clientMessage(pricing.getClientMessage())
                .createdAt(saved != null ? saved.getCreatedAt() : null)
                .build();
    }

    private AiEstimateResponse toResponse(SavedAiEstimate e) {
        return AiEstimateResponse.builder()
                .savedEstimateId(e.getId())
                .projectName(e.getProjectName())
                .projectDescription(e.getProjectDescription())
                .platform(e.getPlatform())
                .estimatedScreenCount(e.getEstimatedScreenCount())
                .features(e.getFeatures())
                .totalExpectedDays(e.getTotalExpectedDays())
                .finalAmount(e.getFinalAmount())
                .schedule(e.getSchedule())
                .risks(e.getRisks())
                .breakdown(e.getBreakdown())
                .clientMessage(e.getClientMessage())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private void validateAnalysis(AiEstimateAnalysis analysis) {
        if (analysis == null) {
            throw new IllegalArgumentException("AI 견적 분석 결과가 비어 있습니다.");
        }
        if (analysis.estimatedScreenCount == null || analysis.estimatedScreenCount < 1) {
            analysis.estimatedScreenCount = 1;
        }
    }

    private String nonBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }
}
