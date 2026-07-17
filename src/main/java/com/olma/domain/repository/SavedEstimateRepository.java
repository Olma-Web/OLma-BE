package com.olma.domain.repository;

import com.olma.domain.entity.SavedEstimate;
import com.olma.domain.enums.NegotiationSimulationStatus;
import com.olma.domain.enums.PlatformEnvironment;
import com.olma.domain.enums.UxEngagement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedEstimateRepository extends JpaRepository<SavedEstimate, Long> {
    List<SavedEstimate> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
    List<SavedEstimate> findAllByUser_IdAndNegotiationSimulationStatusOrderByCreatedAtDesc(
            Long userId,
            NegotiationSimulationStatus negotiationSimulationStatus
    );
    List<SavedEstimate> findAllByUser_IdAndNegotiationSimulationStatusNotOrderByCreatedAtDesc(
            Long userId,
            NegotiationSimulationStatus negotiationSimulationStatus
    );
    List<SavedEstimate> findAllByUser_IdAndExperienceLevel_IdAndJobCategory_IdAndScreenCountAndUxEngagementAndPlatformEnvironmentAndAddonPercentAndFinalAmountOrderByCreatedAtDesc(
            Long userId,
            Long experienceLevelId,
            Long jobCategoryId,
            Integer screenCount,
            UxEngagement uxEngagement,
            PlatformEnvironment platformEnvironment,
            Integer addonPercent,
            Integer finalAmount
    );
    void deleteAllByUser_Id(Long userId);
}
