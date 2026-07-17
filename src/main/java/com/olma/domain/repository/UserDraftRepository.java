package com.olma.domain.repository;

import com.olma.domain.entity.UserDraft;
import com.olma.domain.enums.UserDraftType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDraftRepository extends JpaRepository<UserDraft, Long> {
    List<UserDraft> findAllByUser_IdOrderByUpdatedAtDesc(Long userId);

    Optional<UserDraft> findByUser_IdAndType(Long userId, UserDraftType type);

    void deleteByUser_IdAndType(Long userId, UserDraftType type);
}
