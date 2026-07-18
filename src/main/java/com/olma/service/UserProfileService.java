package com.olma.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.olma.domain.entity.*;
import com.olma.domain.enums.SubmissionStatus;
import com.olma.domain.repository.*;
import com.olma.dto.ChangePasswordRequest;
import com.olma.dto.ProfileSpecProgressRequest;
import com.olma.dto.SubmissionTimelineItem;
import com.olma.dto.UserProfileResponse;
import com.olma.dto.UserProfileUpdateRequest;
import com.olma.exception.InvalidCredentialsException;
import com.olma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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

        user.completeProfileSpec(toProfileSpecState(request));

        log.info("user profile updated userId={}", userId);
        return toResponse(user, newCerts);
    }

    @Transactional
    public UserProfileResponse updateProfileSpecProgress(Long userId, ProfileSpecProgressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        user.updateProfileSpecProgress(request.getState());
        List<UserCertificate> certs = userCertificateRepository.findAllByUser_Id(userId);
        log.info("user profile spec progress saved userId={}", userId);
        return toResponse(user, certs);
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
                .profileSpecStatus(user.getProfileSpecStatus())
                .profileSpecState(user.getProfileSpecState())
                .profileSpecStartedAt(user.getProfileSpecStartedAt())
                .profileSpecUpdatedAt(user.getProfileSpecUpdatedAt())
                .profileSpecCompletedAt(user.getProfileSpecCompletedAt())
                .build();
    }

    private JsonNode toProfileSpecState(UserProfileUpdateRequest request) {
        ObjectNode state = JsonNodeFactory.instance.objectNode();
        putNullableLong(state, "jobCategoryId", request.getJobCategoryId());
        putNullableLong(state, "experienceLevelId", request.getExperienceLevelId());

        ArrayNode certificateIds = state.putArray("certificateTypeIds");
        if (request.getCertificateTypeIds() != null) {
            request.getCertificateTypeIds().forEach(certificateIds::add);
        }
        return state;
    }

    private void putNullableLong(ObjectNode node, String fieldName, Long value) {
        if (value == null) {
            node.putNull(fieldName);
        } else {
            node.put(fieldName, value);
        }
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("현재 비밀번호가 올바르지 않습니다.");
        }
        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        user.increaseTokenVersion();

        log.info("password changed userId={}", userId);
    }

    private SubmissionTimelineItem toTimelineItem(RateSubmission s) {
        return SubmissionTimelineItem.builder()
                .id(s.getId())
                .projectName(s.getProjectName())
                .submissionType(s.getSubmissionType())
                .workFormat(s.getWorkFormat())
                .duration(s.getDuration())
                .amount(s.getAmount())
                .amountUnit(s.getAmountUnit())
                .normalizedMonthly(s.getNormalizedMonthly())
                .jobCategoryName(s.getJobCategory().getName())
                .experienceLevelLabel(s.getExperienceLevel().getLabel())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
