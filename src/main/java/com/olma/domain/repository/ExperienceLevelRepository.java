package com.olma.domain.repository;

import com.olma.domain.entity.ExperienceLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienceLevelRepository extends JpaRepository<ExperienceLevel, Long> {
    List<ExperienceLevel> findAllByOrderByDisplayOrder();
}
