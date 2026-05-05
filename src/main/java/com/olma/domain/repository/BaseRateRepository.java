package com.olma.domain.repository;

import com.olma.domain.entity.BaseRate;
import com.olma.domain.entity.BaseRateId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BaseRateRepository extends JpaRepository<BaseRate, BaseRateId> {
    Optional<BaseRate> findByExperienceLevel_IdAndJobCategory_Id(Long experienceLevelId, Long jobCategoryId);
}
