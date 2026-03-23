package com.olma.service;

import com.olma.domain.entity.*;
import com.olma.domain.repository.*;
import com.olma.dto.RateSubmissionRequest;
import com.olma.dto.RateSubmissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RateSubmissionService {

    private final RateSubmissionRepository rateSubmissionRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final WorkTypeRepository workTypeRepository;
    private final RegionRepository regionRepository;
    private final ExperienceLevelRepository experienceLevelRepository;

    @Transactional
    public RateSubmissionResponse create(RateSubmissionRequest request) {
        JobCategory jobCategory = jobCategoryRepository.findById(request.getJobCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid job category"));
        WorkType workType = workTypeRepository.findById(request.getWorkTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid work type"));
        Region region = request.getRegionId() != null
                ? regionRepository.findById(request.getRegionId()).orElse(null)
                : null;
        ExperienceLevel experienceLevel = experienceLevelRepository.findById(request.getExperienceLevelId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid experience level"));

        RateSubmission submission = RateSubmission.builder()
                .jobCategory(jobCategory)
                .workType(workType)
                .region(region)
                .experienceLevel(experienceLevel)
                .submissionType(request.getSubmissionType())
                .isRemote(request.getIsRemote())
                .complexity(request.getComplexity())
                .durationMonths(request.getDurationMonths())
                .amount(request.getAmount())
                .amountUnit(request.getAmountUnit())
                .sessionId(request.getSessionId())
                .build();

        submission = rateSubmissionRepository.save(submission);
        return toResponse(submission);
    }

    @Transactional(readOnly = true)
    public RateSubmissionResponse getById(Long id) {
        RateSubmission submission = rateSubmissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        return toResponse(submission);
    }

    private RateSubmissionResponse toResponse(RateSubmission s) {
        return RateSubmissionResponse.builder()
                .id(s.getId())
                .jobCategoryName(s.getJobCategory().getName())
                .workTypeName(s.getWorkType().getName())
                .regionName(s.getRegion() != null ? s.getRegion().getName() : null)
                .experienceLevelLabel(s.getExperienceLevel().getLabel())
                .submissionType(s.getSubmissionType())
                .isRemote(s.getIsRemote())
                .complexity(s.getComplexity())
                .durationMonths(s.getDurationMonths())
                .amount(s.getAmount())
                .amountUnit(s.getAmountUnit())
                .normalizedMonthly(s.getNormalizedMonthly())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
