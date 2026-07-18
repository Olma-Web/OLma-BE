package com.olma.service;

import com.olma.domain.entity.ExperienceLevel;
import com.olma.domain.entity.JobCategory;
import com.olma.domain.entity.RateSubmission;
import com.olma.domain.entity.User;
import com.olma.domain.enums.AmountUnit;
import com.olma.domain.enums.SubmissionStatus;
import com.olma.domain.enums.SubmissionType;
import com.olma.domain.enums.WorkFormat;
import com.olma.domain.repository.ExperienceLevelRepository;
import com.olma.domain.repository.JobCategoryRepository;
import com.olma.domain.repository.RateSubmissionRepository;
import com.olma.domain.repository.UserRepository;
import com.olma.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateSubmissionServiceTest {

    private final RateSubmissionRepository rateSubmissionRepository = mock(RateSubmissionRepository.class);
    private final JobCategoryRepository jobCategoryRepository = mock(JobCategoryRepository.class);
    private final ExperienceLevelRepository experienceLevelRepository = mock(ExperienceLevelRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final RateSubmissionService service = new RateSubmissionService(
            rateSubmissionRepository,
            jobCategoryRepository,
            experienceLevelRepository,
            userRepository
    );

    @Test
    void getByIdOnlyReturnsActiveSubmission() {
        when(rateSubmissionRepository.findByIdAndStatus(10L, SubmissionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Submission not found: id=10");
    }

    @Test
    void deleteHidesOnlyOwnedActiveSubmission() {
        RateSubmission submission = submissionWithOwner(7L);
        ReflectionTestUtils.setField(submission, "id", 10L);
        when(rateSubmissionRepository.findByIdAndUser_IdAndStatus(10L, 7L, SubmissionStatus.ACTIVE))
                .thenReturn(Optional.of(submission));

        service.delete(7L, 10L);

        verify(rateSubmissionRepository).findByIdAndUser_IdAndStatus(10L, 7L, SubmissionStatus.ACTIVE);
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.HIDDEN);
    }

    @Test
    void deleteRejectsSubmissionOwnedByAnotherUser() {
        when(rateSubmissionRepository.findByIdAndUser_IdAndStatus(10L, 7L, SubmissionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(7L, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Submission not found: id=10");

        verify(rateSubmissionRepository, never()).findById(10L);
    }

    private RateSubmission submissionWithOwner(Long userId) {
        User user = User.builder()
                .email("user" + userId + "@example.com")
                .password("password")
                .nickname("tester" + userId)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        JobCategory jobCategory = newInstance(JobCategory.class);
        ReflectionTestUtils.setField(jobCategory, "id", 14L);
        ReflectionTestUtils.setField(jobCategory, "name", "UX/UI 디자인");

        ExperienceLevel experienceLevel = newInstance(ExperienceLevel.class);
        ReflectionTestUtils.setField(experienceLevel, "id", 1L);
        ReflectionTestUtils.setField(experienceLevel, "label", "중급");

        return RateSubmission.builder()
                .jobCategory(jobCategory)
                .experienceLevel(experienceLevel)
                .user(user)
                .submissionType(SubmissionType.TRACK_A)
                .workFormat(WorkFormat.REMOTE)
                .duration("1개월")
                .amount(300)
                .amountUnit(AmountUnit.MONTHLY)
                .sessionId(UUID.randomUUID())
                .projectName("테스트 제보")
                .build();
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
