package com.olma.service;

import com.olma.domain.enums.WorkFormat;
import com.olma.domain.repository.RateSubmissionRepository;
import com.olma.dto.BenchmarkResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BenchmarkService {

    private static final int BUCKET_COUNT = 20;

    private final RateSubmissionRepository rateSubmissionRepository;

    @Transactional(readOnly = true)
    public BenchmarkResult getBenchmark(Long jobCategoryId, Long experienceLevelId,
                                         WorkFormat workFormat, Integer userAmount) {
        String workFormatStr = workFormat != null ? workFormat.name() : null;
        List<BenchmarkResult.ComparisonGroup> comparisonGroups = buildComparisonGroups(
                jobCategoryId, experienceLevelId, workFormatStr, userAmount);

        Stats exactStats = getStats(jobCategoryId, experienceLevelId, workFormatStr, userAmount);
        long n = exactStats.n();
        if (n == 0) {
            return BenchmarkResult.builder()
                    .n(0)
                    .comparisonGroups(comparisonGroups)
                    .build();
        }

        List<Object[]> distRows = rateSubmissionRepository.findDistribution(
                jobCategoryId, experienceLevelId, workFormatStr, BUCKET_COUNT);

        List<BenchmarkResult.DistributionBucket> distribution = distRows.stream()
                .map(this::toBucket)
                .toList();

        return BenchmarkResult.builder()
                .n(n)
                .p10(exactStats.p10())
                .p25(exactStats.p25())
                .median(exactStats.median())
                .p75(exactStats.p75())
                .p90(exactStats.p90())
                .distribution(distribution)
                .userPercentile(exactStats.userPercentile())
                .comparisonGroups(comparisonGroups)
                .build();
    }

    private List<BenchmarkResult.ComparisonGroup> buildComparisonGroups(Long jobCategoryId, Long experienceLevelId,
                                                                        String workFormat, Integer userAmount) {
        return List.of(
                comparisonGroup("EXACT", "현재 조건", jobCategoryId, experienceLevelId, workFormat, userAmount),
                comparisonGroup("WITHOUT_WORK_FORMAT", "근무 형태 제외", jobCategoryId, experienceLevelId, null, userAmount),
                comparisonGroup("WITHOUT_EXPERIENCE", "경력 제외", jobCategoryId, null, workFormat, userAmount),
                comparisonGroup("JOB_CATEGORY_ONLY", "직무 전체", jobCategoryId, null, null, userAmount)
        );
    }

    private BenchmarkResult.ComparisonGroup comparisonGroup(String scope, String label,
                                                            Long jobCategoryId, Long experienceLevelId,
                                                            String workFormat, Integer userAmount) {
        Stats stats = getStats(jobCategoryId, experienceLevelId, workFormat, userAmount);
        return BenchmarkResult.ComparisonGroup.builder()
                .scope(scope)
                .label(label)
                .n(stats.n())
                .p25(stats.p25())
                .median(stats.median())
                .p75(stats.p75())
                .userPercentile(stats.userPercentile())
                .build();
    }

    private Stats getStats(Long jobCategoryId, Long experienceLevelId, String workFormat, Integer userAmount) {
        Object[] stats = rateSubmissionRepository.findBenchmarkStats(
                jobCategoryId, experienceLevelId, workFormat);

        if (stats == null || stats.length == 0) {
            return Stats.empty();
        }

        Object[] row = (stats[0] instanceof Object[]) ? (Object[]) stats[0] : stats;
        long n = ((Number) row[0]).longValue();
        if (n == 0) {
            return Stats.empty();
        }

        Double userPercentile = null;
        if (userAmount != null) {
            long belowCount = rateSubmissionRepository.countBelowOrEqual(
                    jobCategoryId, experienceLevelId, workFormat, userAmount);
            userPercentile = Math.round((double) belowCount / n * 1000.0) / 10.0;
        }

        return new Stats(
                n,
                toInt(row[1]),
                toInt(row[2]),
                toInt(row[3]),
                toInt(row[4]),
                toInt(row[5]),
                userPercentile);
    }

    private BenchmarkResult.DistributionBucket toBucket(Object[] r) {
        Long cohortSize = toLong(r[4]);
        Long certHolders = toLong(r[5]);
        Double certRatio = (cohortSize != null && cohortSize > 0 && certHolders != null)
                ? Math.round(((double) certHolders / cohortSize) * 1000.0) / 1000.0
                : null;
        return BenchmarkResult.DistributionBucket.builder()
                .bucket(((Number) r[0]).intValue())
                .rangeStart(((Number) r[1]).intValue())
                .rangeEnd(((Number) r[2]).intValue())
                .count(((Number) r[3]).longValue())
                .cohortSize(cohortSize)
                .certHoldersCount(certHolders)
                .certRatio(certRatio)
                .mostCommonDuration(r[6] != null ? r[6].toString() : null)
                .build();
    }

    private Integer toInt(Object val) {
        return val != null ? ((Number) val).intValue() : null;
    }

    private Long toLong(Object val) {
        return val != null ? ((Number) val).longValue() : null;
    }

    private record Stats(long n, Integer p10, Integer p25, Integer median, Integer p75, Integer p90,
                         Double userPercentile) {
        private static Stats empty() {
            return new Stats(0, null, null, null, null, null, null);
        }
    }
}
