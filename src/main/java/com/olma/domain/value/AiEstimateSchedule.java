package com.olma.domain.value;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiEstimateSchedule {
    private Integer p50Days;
    private Integer p80Days;
    private Integer p95Days;
    private Integer simulationCount;
}
