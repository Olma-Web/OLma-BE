package com.olma.domain.repository;

import com.olma.domain.entity.SavedAiEstimate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedAiEstimateRepository extends JpaRepository<SavedAiEstimate, Long> {
    List<SavedAiEstimate> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
    void deleteAllByUser_Id(Long userId);
}
