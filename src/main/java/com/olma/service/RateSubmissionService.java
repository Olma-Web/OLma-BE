package com.olma.service;

import com.olma.domain.entity.ExperienceLevel;
import com.olma.domain.entity.JobCategory;
import com.olma.domain.entity.RateSubmission;
import com.olma.domain.entity.User;
import com.olma.domain.repository.ExperienceLevelRepository;
import com.olma.domain.repository.JobCategoryRepository;
import com.olma.domain.repository.RateSubmissionRepository;
import com.olma.domain.repository.UserRepository;
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
    private final ExperienceLevelRepository experienceLevelRepository;
    private final UserRepository userRepository;

    @Transactional
    public RateSubmissionResponse create(RateSubmissionRequest request) {
        JobCategory jobCategory = jobCategoryRepository.findById(request.getJobCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid job category"));
        ExperienceLevel experienceLevel = experienceLevelRepository.findById(request.getExperienceLevelId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid experience level"));
        User user = request.getUserId() != null
                ? userRepository.findById(request.getUserId()).orElse(null)
                : null;

        RateSubmission submission = RateSubmission.builder()
                .jobCategory(jobCategory)
                .experienceLevel(experienceLevel)
                .user(user)
                .submissionType(request.getSubmissionType())
                .workFormat(request.getWorkFormat())
                .duration(request.getDuration())
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

    @Transactional
    public void delete(Long id) {
        RateSubmission submission = rateSubmissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        submission.hide();
    }

    private RateSubmissionResponse toResponse(RateSubmission s) {
        return RateSubmissionResponse.builder()
                .id(s.getId())
                .jobCategoryName(s.getJobCategory().getName())
                .experienceLevelLabel(s.getExperienceLevel().getLabel())
                .submissionType(s.getSubmissionType())
                .workFormat(s.getWorkFormat())
                .duration(s.getDuration())
                .amount(s.getAmount())
                .amountUnit(s.getAmountUnit())
                .normalizedMonthly(s.getNormalizedMonthly())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
