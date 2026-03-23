package com.olma.domain.repository;

import com.olma.domain.entity.WorkType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkTypeRepository extends JpaRepository<WorkType, Long> {
    List<WorkType> findAllByOrderByDisplayOrder();
}
