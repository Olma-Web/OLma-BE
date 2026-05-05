package com.olma.controller;

import com.olma.domain.enums.WorkFormat;
import com.olma.dto.BenchmarkResult;
import com.olma.service.BenchmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/benchmark")
@RequiredArgsConstructor
public class BenchmarkController {

    private final BenchmarkService benchmarkService;

    @GetMapping
    public BenchmarkResult getBenchmark(
            @RequestParam Long jobCategoryId,
            @RequestParam(required = false) Long experienceLevelId,
            @RequestParam(required = false) WorkFormat workFormat,
            @RequestParam(required = false) Integer userAmount) {
        return benchmarkService.getBenchmark(
                jobCategoryId, experienceLevelId, workFormat, userAmount);
    }
}
