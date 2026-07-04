package com.olma.service;

import com.olma.dto.AiEstimateAnalysis;
import com.olma.dto.AiEstimateCalculateRequest;

public interface AiEstimateAnalyzer {
    AiEstimateAnalysis analyze(AiEstimateCalculateRequest request);
}
