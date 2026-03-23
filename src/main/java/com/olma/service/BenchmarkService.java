package com.olma.service;

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
    public BenchmarkResult getBenchmark(Long jobCategoryId, Long workTypeId,
                                         Long experienceLevelId, Boolean isRemote,
                                         String complexity, Integer userAmount) {
        Object[] stats = rateSubmissionRepository.findBenchmarkStats(
                jobCategoryId, workTypeId, experienceLevelId, isRemote, complexity);

        if (stats == null || stats.length == 0) {
            return BenchmarkResult.builder().n(0).build();
        }

        Object[] row = (stats[0] instanceof Object[]) ? (Object[]) stats[0] : stats;

        long n = ((Number) row[0]).longValue();
        if (n == 0) {
            return BenchmarkResult.builder().n(0).build();
        }

        Integer p10 = toInt(row[1]);
        Integer p25 = toInt(row[2]);
        Integer median = toInt(row[3]);
        Integer p75 = toInt(row[4]);
        Integer p90 = toInt(row[5]);

        List<Object[]> distRows = rateSubmissionRepository.findDistribution(
                jobCategoryId, workTypeId, experienceLevelId, isRemote, complexity, BUCKET_COUNT);

        List<BenchmarkResult.DistributionBucket> distribution = distRows.stream()
                .map(r -> BenchmarkResult.DistributionBucket.builder()
                        .bucket(((Number) r[0]).intValue())
                        .rangeStart(((Number) r[1]).intValue())
                        .rangeEnd(((Number) r[2]).intValue())
                        .count(((Number) r[3]).longValue())
                        .build())
                .toList();

        Double userPercentile = null;
        if (userAmount != null) {
            long belowCount = rateSubmissionRepository.countBelowOrEqual(
                    jobCategoryId, workTypeId, experienceLevelId, isRemote, complexity, userAmount);
            userPercentile = Math.round((double) belowCount / n * 1000.0) / 10.0;
        }

        return BenchmarkResult.builder()
                .n(n)
                .p10(p10)
                .p25(p25)
                .median(median)
                .p75(p75)
                .p90(p90)
                .distribution(distribution)
                .userPercentile(userPercentile)
                .build();
    }

    private Integer toInt(Object val) {
        return val != null ? ((Number) val).intValue() : null;
    }
}
