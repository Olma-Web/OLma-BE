package com.olma.service;

import com.olma.domain.entity.ExperienceLevel;
import com.olma.domain.entity.JobCategory;
import com.olma.domain.entity.RateSubmission;
import com.olma.domain.entity.User;
import com.olma.domain.enums.SubmissionStatus;
import com.olma.domain.repository.ExperienceLevelRepository;
import com.olma.domain.repository.JobCategoryRepository;
import com.olma.domain.repository.RateSubmissionRepository;
import com.olma.domain.repository.UserRepository;
import com.olma.dto.ProjectNameUpdateRequest;
import com.olma.dto.RateSubmissionRequest;
import com.olma.dto.RateSubmissionResponse;
import com.olma.exception.ForbiddenException;
import com.olma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
                .projectName(request.getProjectName())
                .build();

        Integer normalized = submission.getNormalizedMonthly();
        if (normalized != null && (normalized < 10 || normalized > 9999)) {
            throw new IllegalArgumentException("환산 월 단가는 10~9,999만원 범위여야 합니다.");
        }

        submission = rateSubmissionRepository.save(submission);
        log.info("rate submission created submissionId={} userId={}", submission.getId(), request.getUserId());
        return toResponse(submission);
    }

    @Transactional(readOnly = true)
    public RateSubmissionResponse getById(Long id) {
        RateSubmission submission = rateSubmissionRepository.findByIdAndStatus(id, SubmissionStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Submission not found: id=" + id));
        return toResponse(submission);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        RateSubmission submission = rateSubmissionRepository.findByIdAndStatus(id, SubmissionStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Submission not found: id=" + id));
        validateOwner(submission, userId);
        submission.hide();
        log.info("rate submission hidden submissionId={} userId={}", id, userId);
    }

    @Transactional
    public RateSubmissionResponse updateProjectName(Long userId, Long submissionId, ProjectNameUpdateRequest request) {
        RateSubmission submission = rateSubmissionRepository.findByIdAndStatus(submissionId, SubmissionStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Submission not found: id=" + submissionId));
        validateOwner(submission, userId);
        submission.updateProjectName(request.getProjectName());
        log.info("rate submission project name updated submissionId={} userId={}", submissionId, userId);
        return toResponse(submission);
    }

    private void validateOwner(RateSubmission submission, Long userId) {
        if (submission.getUser() == null || !submission.getUser().getId().equals(userId)) {
            throw new ForbiddenException("본인이 생성한 단가 제보만 수정하거나 삭제할 수 있습니다.");
        }
    }

    private RateSubmissionResponse toResponse(RateSubmission s) {
        return RateSubmissionResponse.builder()
                .id(s.getId())
                .projectName(s.getProjectName())
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
