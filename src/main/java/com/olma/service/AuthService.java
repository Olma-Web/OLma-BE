package com.olma.service;

import com.olma.config.JwtProvider;
import com.olma.domain.entity.*;
import com.olma.domain.enums.SubmissionStatus;
import com.olma.domain.repository.*;
import com.olma.dto.AuthLoginRequest;
import com.olma.dto.AuthLoginResponse;
import com.olma.dto.AuthSignupRequest;
import com.olma.dto.AuthSignupResponse;
import com.olma.exception.DuplicateValueException;
import com.olma.exception.InvalidCredentialsException;
import com.olma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserCertificateRepository userCertificateRepository;
    private final ExperienceLevelRepository experienceLevelRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final CertificateTypeRepository certificateTypeRepository;
    private final RateSubmissionRepository rateSubmissionRepository;
    private final SavedEstimateRepository savedEstimateRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthSignupResponse signup(AuthSignupRequest request) {
        // email 검증
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateValueException("email");
        }

        // Nickname unique 검증
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateValueException("nickname");
        }

        ExperienceLevel experienceLevel = request.getExperienceLevelId() != null
                ? experienceLevelRepository.findById(request.getExperienceLevelId())
                  .orElseThrow(() -> new NotFoundException("ExperienceLevel not found: id=" + request.getExperienceLevelId()))
                : null;

        JobCategory jobCategory = request.getJobCategoryId() != null
                ? jobCategoryRepository.findById(request.getJobCategoryId())
                  .orElseThrow(() -> new NotFoundException("JobCategory not found: id=" + request.getJobCategoryId()))
                : null;

        User user = userRepository.save(User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .experienceLevel(experienceLevel)
                .jobCategory(jobCategory)
                .build());

        if (request.getCertificateTypeIds() != null && !request.getCertificateTypeIds().isEmpty()) {
            List<Long> ids = request.getCertificateTypeIds();
            List<CertificateType> types = certificateTypeRepository.findAllById(ids);
            if (ids.size() != types.size()) {
                throw new NotFoundException("CertificateType not found");
            }
            List<UserCertificate> certificates = types.stream().map(t -> UserCertificate.builder().user(user).certificateType(t).build()).toList();
            userCertificateRepository.saveAll(certificates);
        }

        return AuthSignupResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .agreementAt(user.getAgreementAt())
                .token(jwtProvider.generateJwtToken(user.getId()))
                .build();
    }

    @Transactional
    public AuthLoginResponse login(AuthLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        return AuthLoginResponse.builder()
                .id(user.getId())
                .token(jwtProvider.generateJwtToken(user.getId()))
                .build();
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));

        rateSubmissionRepository.updateStatusByUserId(userId, SubmissionStatus.HIDDEN);
        savedEstimateRepository.deleteAllByUser_Id(userId);
        userCertificateRepository.deleteAllByUser_Id(userId);
        userRepository.delete(user);
    }
}
