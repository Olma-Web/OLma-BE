package com.olma.domain.repository;

import com.olma.domain.entity.SavedEstimate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedEstimateRepository extends JpaRepository<SavedEstimate, Long> {
    List<SavedEstimate> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
}
