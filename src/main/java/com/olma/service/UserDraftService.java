package com.olma.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.olma.domain.entity.User;
import com.olma.domain.entity.UserDraft;
import com.olma.domain.enums.UserDraftType;
import com.olma.domain.repository.UserDraftRepository;
import com.olma.domain.repository.UserRepository;
import com.olma.dto.UserDraftResponse;
import com.olma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDraftService {

    private final UserRepository userRepository;
    private final UserDraftRepository userDraftRepository;

    @Transactional(readOnly = true)
    public List<UserDraftResponse> getDrafts(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: id=" + userId);
        }
        return userDraftRepository.findAllByUser_IdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDraftResponse getDraft(Long userId, UserDraftType type) {
        return toResponse(getOwnedDraft(userId, type));
    }

    @Transactional
    public UserDraftResponse saveProgress(Long userId, UserDraftType type, JsonNode state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        UserDraft draft = userDraftRepository.findByUser_IdAndType(userId, type)
                .orElseGet(() -> userDraftRepository.save(UserDraft.builder()
                        .user(user)
                        .type(type)
                        .state(JsonNodeFactory.instance.objectNode())
                        .build()));
        draft.saveProgress(state);
        log.info("user draft progress saved userId={} type={}", userId, type);
        return toResponse(draft);
    }

    @Transactional
    public UserDraftResponse complete(Long userId, UserDraftType type, JsonNode state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        UserDraft draft = userDraftRepository.findByUser_IdAndType(userId, type)
                .orElseGet(() -> userDraftRepository.save(UserDraft.builder()
                        .user(user)
                        .type(type)
                        .state(JsonNodeFactory.instance.objectNode())
                        .build()));
        draft.complete(state);
        log.info("user draft completed userId={} type={}", userId, type);
        return toResponse(draft);
    }

    @Transactional
    public void deleteDraft(Long userId, UserDraftType type) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: id=" + userId);
        }
        userDraftRepository.deleteByUser_IdAndType(userId, type);
        log.info("user draft deleted userId={} type={}", userId, type);
    }

    private UserDraft getOwnedDraft(Long userId, UserDraftType type) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: id=" + userId);
        }
        return userDraftRepository.findByUser_IdAndType(userId, type)
                .orElseThrow(() -> new NotFoundException("Draft not found: type=" + type));
    }

    private UserDraftResponse toResponse(UserDraft draft) {
        return UserDraftResponse.builder()
                .id(draft.getId())
                .type(draft.getType())
                .status(draft.getStatus())
                .state(draft.getState())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .completedAt(draft.getCompletedAt())
                .build();
    }
}
