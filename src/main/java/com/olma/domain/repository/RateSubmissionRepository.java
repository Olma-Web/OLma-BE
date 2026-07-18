package com.olma.domain.repository;

import com.olma.domain.entity.RateSubmission;
import com.olma.domain.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RateSubmissionRepository extends JpaRepository<RateSubmission, Long> {

    List<RateSubmission> findAllByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, SubmissionStatus status);

    Optional<RateSubmission> findByIdAndStatus(Long id, SubmissionStatus status);

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
              AND (:experienceLevelId IS NULL OR experience_level_id = :experienceLevelId)
              AND (:workFormat IS NULL OR work_format = :workFormat)
            """, nativeQuery = true)
    Object[] findBenchmarkStats(
            @Param("jobCategoryId") Long jobCategoryId,
            @Param("experienceLevelId") Long experienceLevelId,
            @Param("workFormat") String workFormat
    );

    @Query(value = """
            WITH bounds AS (
                SELECT
                    MIN(normalized_monthly) AS min_val,
                    MAX(normalized_monthly) AS max_val
                FROM rate_submissions
                WHERE status = 'ACTIVE'
                  AND is_outlier = false
                  AND normalized_monthly IS NOT NULL
                  AND job_category_id = :jobCategoryId
                  AND (:experienceLevelId IS NULL OR experience_level_id = :experienceLevelId)
                  AND (:workFormat IS NULL OR work_format = :workFormat)
            ),
            bucketed AS (
                SELECT
                    width_bucket(r.normalized_monthly, b.min_val, b.max_val + 1, :bucketCount) AS bucket,
                    r.user_id,
                    r.duration,
                    r.amount_unit,
                    b.min_val,
                    b.max_val
                FROM rate_submissions r, bounds b
                WHERE r.status = 'ACTIVE'
                  AND r.is_outlier = false
                  AND r.normalized_monthly IS NOT NULL
                  AND r.job_category_id = :jobCategoryId
                  AND (:experienceLevelId IS NULL OR r.experience_level_id = :experienceLevelId)
                  AND (:workFormat IS NULL OR r.work_format = :workFormat)
            )
            SELECT
                bucket,
                MIN(min_val) + (bucket - 1) * (MIN(max_val) - MIN(min_val)) / :bucketCount AS range_start,
                MIN(min_val) + bucket * (MIN(max_val) - MIN(min_val)) / :bucketCount AS range_end,
                count(*) AS count,
                count(DISTINCT user_id) FILTER (WHERE user_id IS NOT NULL) AS cohort_size,
                count(DISTINCT user_id) FILTER (
                    WHERE user_id IS NOT NULL
                      AND EXISTS (SELECT 1 FROM user_certificates uc WHERE uc.user_id = bucketed.user_id)
                ) AS cert_holders_count,
                mode() WITHIN GROUP (ORDER BY duration) FILTER (WHERE amount_unit = 'TOTAL') AS most_common_duration
            FROM bucketed
            GROUP BY bucket
            ORDER BY bucket
            """, nativeQuery = true)
    List<Object[]> findDistribution(
            @Param("jobCategoryId") Long jobCategoryId,
            @Param("experienceLevelId") Long experienceLevelId,
            @Param("workFormat") String workFormat,
            @Param("bucketCount") int bucketCount
    );

    @Query(value = """
            SELECT count(*)
            FROM rate_submissions
            WHERE status = 'ACTIVE'
              AND is_outlier = false
              AND job_category_id = :jobCategoryId
              AND (:experienceLevelId IS NULL OR experience_level_id = :experienceLevelId)
              AND (:workFormat IS NULL OR work_format = :workFormat)
              AND normalized_monthly <= :normalizedMonthly
            """, nativeQuery = true)
    long countBelowOrEqual(
            @Param("jobCategoryId") Long jobCategoryId,
            @Param("experienceLevelId") Long experienceLevelId,
            @Param("workFormat") String workFormat,
            @Param("normalizedMonthly") Integer normalizedMonthly
    );

    @Modifying
    @Query("UPDATE RateSubmission r SET r.status = :status WHERE r.user.id = :userId")
    void updateStatusByUserId(@Param("userId") Long userId, @Param("status") SubmissionStatus status);

    @Modifying
    @Query(value = """
            WITH percentiles AS (
                SELECT
                    job_category_id,
                    experience_level_id,
                    percentile_cont(0.05) WITHIN GROUP (ORDER BY normalized_monthly) AS p5,
                    percentile_cont(0.95) WITHIN GROUP (ORDER BY normalized_monthly) AS p95
                FROM rate_submissions
                WHERE status = 'ACTIVE' AND normalized_monthly IS NOT NULL
                GROUP BY job_category_id, experience_level_id
                HAVING count(*) >= :minThreshold
            )
            UPDATE rate_submissions r
            SET is_outlier = (r.normalized_monthly < p.p5 OR r.normalized_monthly > p.p95)
            FROM percentiles p
            WHERE r.job_category_id = p.job_category_id
              AND r.experience_level_id = p.experience_level_id
              AND r.status = 'ACTIVE'
              AND r.normalized_monthly IS NOT NULL
            """, nativeQuery = true)
    int markOutliers(@Param("minThreshold") int minThreshold);
}
