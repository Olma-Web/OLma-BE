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
    private List<ComparisonGroup> comparisonGroups;

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

    @Getter
    @Builder
    public static class ComparisonGroup {
        private String scope;
        private String label;
        private long n;
        private Integer p25;
        private Integer median;
        private Integer p75;
        private Double userPercentile;
    }
}
