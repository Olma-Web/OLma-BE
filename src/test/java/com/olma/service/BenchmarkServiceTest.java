package com.olma.service;

import com.olma.domain.enums.WorkFormat;
import com.olma.domain.repository.RateSubmissionRepository;
import com.olma.dto.BenchmarkResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BenchmarkServiceTest {

    private final RateSubmissionRepository repository = mock(RateSubmissionRepository.class);
    private final BenchmarkService service = new BenchmarkService(repository);

    @Test
    void returnsComparisonGroupsWithFallbackStats() {
        when(repository.findBenchmarkStats(anyLong(), any(), any()))
                .thenReturn(new Object[]{20L, 200, 300, 400, 500, 600});
        when(repository.findDistribution(anyLong(), any(), any(), anyInt()))
                .thenReturn(List.of());
        when(repository.countBelowOrEqual(anyLong(), any(), any(), eq(450)))
                .thenReturn(12L);

        BenchmarkResult result = service.getBenchmark(14L, 3L, WorkFormat.REMOTE, 450);

        assertThat(result.getN()).isEqualTo(20);
        assertThat(result.getMedian()).isEqualTo(400);
        assertThat(result.getUserPercentile()).isEqualTo(60.0);
        assertThat(result.getComparisonGroups())
                .extracting(BenchmarkResult.ComparisonGroup::getScope)
                .containsExactly("EXACT", "WITHOUT_WORK_FORMAT", "WITHOUT_EXPERIENCE", "JOB_CATEGORY_ONLY");
    }

    @Test
    void includesFallbackGroupsEvenWhenExactConditionHasNoData() {
        when(repository.findBenchmarkStats(anyLong(), eq(3L), eq("REMOTE")))
                .thenReturn(new Object[]{0L, null, null, null, null, null});
        when(repository.findBenchmarkStats(anyLong(), eq(3L), eq(null)))
                .thenReturn(new Object[]{18L, 180, 280, 380, 480, 580});
        when(repository.findBenchmarkStats(anyLong(), eq(null), eq("REMOTE")))
                .thenReturn(new Object[]{30L, 160, 260, 360, 460, 560});
        when(repository.findBenchmarkStats(anyLong(), eq(null), eq(null)))
                .thenReturn(new Object[]{60L, 140, 240, 340, 440, 540});

        BenchmarkResult result = service.getBenchmark(14L, 3L, WorkFormat.REMOTE, null);

        assertThat(result.getN()).isZero();
        assertThat(result.getComparisonGroups()).hasSize(4);
        assertThat(result.getComparisonGroups().get(1).getN()).isEqualTo(18);
        assertThat(result.getComparisonGroups().get(3).getMedian()).isEqualTo(340);
    }
}
