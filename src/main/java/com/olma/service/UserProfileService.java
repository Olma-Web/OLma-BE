package com.olma.service;

import com.olma.domain.entity.CertificateType;
import com.olma.domain.entity.ExperienceLevel;
import com.olma.domain.entity.JobCategory;
import com.olma.domain.entity.RateSubmission;
import com.olma.domain.entity.User;
import com.olma.domain.entity.UserCertificate;
import com.olma.domain.enums.SubmissionStatus;
import com.olma.domain.repository.CertificateTypeRepository;
import com.olma.domain.repository.ExperienceLevelRepository;
import com.olma.domain.repository.JobCategoryRepository;
import com.olma.domain.repository.RateSubmissionRepository;
import com.olma.domain.repository.UserCertificateRepository;
import com.olma.domain.repository.UserRepository;
import com.olma.dto.ChangePasswordRequest;
import com.olma.dto.SubmissionTimelineItem;
import com.olma.dto.UserProfileResponse;
import com.olma.dto.UserProfileUpdateRequest;
import com.olma.exception.InvalidCredentialsException;
import com.olma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ExperienceLevelRepository experienceLevelRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final CertificateTypeRepository certificateTypeRepository;
    private final UserCertificateRepository userCertificateRepository;
    private final RateSubmissionRepository rateSubmissionRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        List<UserCertificate> certs = userCertificateRepository.findAllByUser_Id(userId);
        return toResponse(user, certs);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));

        ExperienceLevel experienceLevel = request.getExperienceLevelId() != null
                ? experienceLevelRepository.findById(request.getExperienceLevelId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid experience level"))
                : null;
        JobCategory jobCategory = request.getJobCategoryId() != null
                ? jobCategoryRepository.findById(request.getJobCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid job category"))
                : null;

        user.updateProfile(experienceLevel, jobCategory);

        userCertificateRepository.deleteAllByUser_Id(userId);
        List<UserCertificate> newCerts = List.of();
        if (request.getCertificateTypeIds() != null && !request.getCertificateTypeIds().isEmpty()) {
            List<CertificateType> certTypes = certificateTypeRepository.findAllById(request.getCertificateTypeIds());
            if (certTypes.size() != request.getCertificateTypeIds().size()) {
                throw new IllegalArgumentException("One or more certificate types not found");
            }
            newCerts = certTypes.stream()
                    .map(ct -> UserCertificate.builder().user(user).certificateType(ct).build())
                    .toList();
            userCertificateRepository.saveAll(newCerts);
        }

        return toResponse(user, newCerts);
    }

    @Transactional(readOnly = true)
    public List<SubmissionTimelineItem> getSubmissions(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: id=" + userId);
        }
        return rateSubmissionRepository
                .findAllByUser_IdAndStatusOrderByCreatedAtDesc(userId, SubmissionStatus.ACTIVE)
                .stream()
                .map(this::toTimelineItem)
                .toList();
    }

    private UserProfileResponse toResponse(User user, List<UserCertificate> certs) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .jobCategoryId(user.getJobCategory() != null ? user.getJobCategory().getId() : null)
                .jobCategoryName(user.getJobCategory() != null ? user.getJobCategory().getName() : null)
                .experienceLevelId(user.getExperienceLevel() != null ? user.getExperienceLevel().getId() : null)
                .experienceLevelLabel(user.getExperienceLevel() != null ? user.getExperienceLevel().getLabel() : null)
                .certificates(certs.stream()
                        .map(c -> UserProfileResponse.CertificateInfo.builder()
                                .id(c.getCertificateType().getId())
                                .name(c.getCertificateType().getName())
                                .build())
                        .toList())
                .build();
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("현재 비밀번호가 올바르지 않습니다.");
        }
        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    private SubmissionTimelineItem toTimelineItem(RateSubmission s) {
        return SubmissionTimelineItem.builder()
                .id(s.getId())
                .submissionType(s.getSubmissionType())
                .workFormat(s.getWorkFormat())
                .duration(s.getDuration())
                .amount(s.getAmount())
                .amountUnit(s.getAmountUnit())
                .jobCategoryName(s.getJobCategory().getName())
                .experienceLevelLabel(s.getExperienceLevel().getLabel())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
