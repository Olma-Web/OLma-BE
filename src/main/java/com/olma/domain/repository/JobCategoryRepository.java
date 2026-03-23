package com.olma.domain.repository;

import com.olma.domain.entity.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {

    @Query("SELECT DISTINCT c FROM JobCategory c LEFT JOIN FETCH c.children WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.displayOrder")
    List<JobCategory> findRootsWithChildren();
}
