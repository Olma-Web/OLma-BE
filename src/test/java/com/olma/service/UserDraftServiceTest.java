package com.olma.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.olma.domain.entity.User;
import com.olma.domain.entity.UserDraft;
import com.olma.domain.enums.UserDraftStatus;
import com.olma.domain.enums.UserDraftType;
import com.olma.domain.repository.UserDraftRepository;
import com.olma.domain.repository.UserRepository;
import com.olma.dto.UserDraftResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserDraftServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserDraftRepository userDraftRepository = mock(UserDraftRepository.class);
    private final UserDraftService service = new UserDraftService(userRepository, userDraftRepository);

    @Test
    void saveProgressCreatesDraftWhenMissing() {
        User user = user();
        JsonNode state = JsonNodeFactory.instance.objectNode().put("step", 2);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(userDraftRepository.findByUser_IdAndType(7L, UserDraftType.ESTIMATE)).thenReturn(Optional.empty());
        when(userDraftRepository.save(any(UserDraft.class))).thenAnswer(invocation -> {
            UserDraft draft = invocation.getArgument(0);
            ReflectionTestUtils.setField(draft, "id", 11L);
            return draft;
        });

        UserDraftResponse response = service.saveProgress(7L, UserDraftType.ESTIMATE, state);

        assertThat(response.getId()).isEqualTo(11L);
        assertThat(response.getType()).isEqualTo(UserDraftType.ESTIMATE);
        assertThat(response.getStatus()).isEqualTo(UserDraftStatus.IN_PROGRESS);
        assertThat(response.getState().get("step").asInt()).isEqualTo(2);
    }

    @Test
    void completeMarksExistingDraftCompleted() {
        UserDraft draft = UserDraft.builder()
                .user(user())
                .type(UserDraftType.ONBOARDING)
                .state(JsonNodeFactory.instance.objectNode().put("step", 7))
                .build();
        ReflectionTestUtils.setField(draft, "id", 12L);
        JsonNode finalState = JsonNodeFactory.instance.objectNode().put("done", true);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user()));
        when(userDraftRepository.findByUser_IdAndType(7L, UserDraftType.ONBOARDING))
                .thenReturn(Optional.of(draft));

        UserDraftResponse response = service.complete(7L, UserDraftType.ONBOARDING, finalState);

        assertThat(response.getStatus()).isEqualTo(UserDraftStatus.COMPLETED);
        assertThat(response.getState().get("done").asBoolean()).isTrue();
        assertThat(response.getCompletedAt()).isNotNull();
    }

    @Test
    void deleteDraftDeletesByUserAndType() {
        when(userRepository.existsById(7L)).thenReturn(true);

        service.deleteDraft(7L, UserDraftType.ESTIMATE);

        verify(userDraftRepository).deleteByUser_IdAndType(7L, UserDraftType.ESTIMATE);
    }

    private User user() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .nickname("tester")
                .build();
        ReflectionTestUtils.setField(user, "id", 7L);
        return user;
    }
}
