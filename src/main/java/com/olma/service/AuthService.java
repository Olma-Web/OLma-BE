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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Slf4j
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
    private final PlatformTransactionManager transactionManager;

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

        // CPU 연산(BCrypt)은 DB 커넥션을 붙잡지 않은 상태에서 실행
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 원자성이 필요한 쓰기(유저 저장 + 자격증 저장)만 짧게 트랜잭션으로 감쌈
        User user = new TransactionTemplate(transactionManager).execute(status -> {
            User saved = userRepository.save(User.builder()
                    .email(request.getEmail())
                    .password(encodedPassword)
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
                List<UserCertificate> certificates = types.stream().map(t -> UserCertificate.builder().user(saved).certificateType(t).build()).toList();
                userCertificateRepository.saveAll(certificates);
            }
            return saved;
        });

        log.info("user signed up userId={}", user.getId());
        return AuthSignupResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .agreementAt(user.getAgreementAt())
                .token(jwtProvider.generateJwtToken(user.getId()))
                .build();
    }

    public AuthLoginResponse login(AuthLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("login failed reason=INVALID_CREDENTIALS");
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
