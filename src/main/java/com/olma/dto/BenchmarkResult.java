package com.olma.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BenchmarkResult {
    private long n;
    private Integer p10;
    private Integer p25;
    private Integer median;
    private Integer p75;
    private Integer p90;
    private List<DistributionBucket> distribution;
    private Double userPercentile;

    @Getter
    @Builder
    public static class DistributionBucket {
        private int bucket;
        private int rangeStart;
        private int rangeEnd;
        private long count;
        private Long cohortSize;
        private Long certHoldersCount;
        private Double certRatio;
        private String mostCommonDuration;
    }
}
