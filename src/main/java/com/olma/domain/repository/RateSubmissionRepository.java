package com.olma.domain.repository;

import com.olma.domain.entity.RateSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RateSubmissionRepository extends JpaRepository<RateSubmission, Long> {

    @Query(value = """
            SELECT
                count(*) AS n,
                percentile_cont(0.10) WITHIN GROUP (ORDER BY normalized_monthly) AS p10,
                percentile_cont(0.25) WITHIN GROUP (ORDER BY normalized_monthly) AS p25,
                percentile_cont(0.50) WITHIN GROUP (ORDER BY normalized_monthly) AS median,
                percentile_cont(0.75) WITHIN GROUP (ORDER BY normalized_monthly) AS p75,
                percentile_cont(0.90) WITHIN GROUP (ORDER BY normalized_monthly) AS p90
            FROM rate_submissions
            WHERE status = 'ACTIVE'
              AND is_outlier = false
              AND job_category_id = :jobCategoryId
              AND (:workTypeId IS NULL OR work_type_id = :workTypeId)
              AND (:experienceLevelId IS NULL OR experience_level_id = :experienceLevelId)
              AND (:isRemote IS NULL OR is_remote = :isRemote)
              AND (:complexity IS NULL OR complexity = :complexity)
            """, nativeQuery = true)
    Object[] findBenchmarkStats(
            @Param("jobCategoryId") Long jobCategoryId,
            @Param("workTypeId") Long workTypeId,
            @Param("experienceLevelId") Long experienceLevelId,
            @Param("isRemote") Boolean isRemote,
            @Param("complexity") String complexity
    );

    @Query(value = """
            WITH bounds AS (
                SELECT
                    MIN(normalized_monthly) AS min_val,
                    MAX(normalized_monthly) AS max_val
                FROM rate_submissions
                WHERE status = 'ACTIVE'
                  AND is_outlier = false
                  AND job_category_id = :jobCategoryId
                  AND (:workTypeId IS NULL OR work_type_id = :workTypeId)
                  AND (:experienceLevelId IS NULL OR experience_level_id = :experienceLevelId)
                  AND (:isRemote IS NULL OR is_remote = :isRemote)
                  AND (:complexity IS NULL OR complexity = :complexity)
            ),
            bucketed AS (
                SELECT
                    width_bucket(r.normalized_monthly, b.min_val, b.max_val + 1, :bucketCount) AS bucket,
                    b.min_val,
                    b.max_val
                FROM rate_submissions r, bounds b
                WHERE r.status = 'ACTIVE'
                  AND r.is_outlier = false
                  AND r.job_category_id = :jobCategoryId
                  AND (:workTypeId IS NULL OR r.work_type_id = :workTypeId)
                  AND (:experienceLevelId IS NULL OR r.experience_level_id = :experienceLevelId)
                  AND (:isRemote IS NULL OR r.is_remote = :isRemote)
                  AND (:complexity IS NULL OR r.complexity = :complexity)
            )
            SELECT
                bucket,
                min_val + (bucket - 1) * (max_val - min_val) / :bucketCount AS range_start,
                min_val + bucket * (max_val - min_val) / :bucketCount AS range_end,
                count(*) AS count
            FROM bucketed
            GROUP BY bucket, min_val, max_val
            ORDER BY bucket
            """, nativeQuery = true)
    List<Object[]> findDistribution(
            @Param("jobCategoryId") Long jobCategoryId,
            @Param("workTypeId") Long workTypeId,
            @Param("experienceLevelId") Long experienceLevelId,
            @Param("isRemote") Boolean isRemote,
            @Param("complexity") String complexity,
            @Param("bucketCount") int bucketCount
    );

    @Query(value = """
            SELECT count(*)
            FROM rate_submissions
            WHERE status = 'ACTIVE'
              AND is_outlier = false
              AND job_category_id = :jobCategoryId
              AND (:workTypeId IS NULL OR work_type_id = :workTypeId)
              AND (:experienceLevelId IS NULL OR experience_level_id = :experienceLevelId)
              AND (:isRemote IS NULL OR is_remote = :isRemote)
              AND (:complexity IS NULL OR complexity = :complexity)
              AND normalized_monthly <= :normalizedMonthly
            """, nativeQuery = true)
    long countBelowOrEqual(
            @Param("jobCategoryId") Long jobCategoryId,
            @Param("workTypeId") Long workTypeId,
            @Param("experienceLevelId") Long experienceLevelId,
            @Param("isRemote") Boolean isRemote,
            @Param("complexity") String complexity,
            @Param("normalizedMonthly") Integer normalizedMonthly
    );
}
