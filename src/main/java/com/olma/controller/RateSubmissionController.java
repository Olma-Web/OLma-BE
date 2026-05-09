package com.olma.controller;

import com.olma.dto.RateSubmissionRequest;
import com.olma.dto.RateSubmissionResponse;
import com.olma.service.RateSubmissionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/submissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class RateSubmissionController {

    private final RateSubmissionService rateSubmissionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RateSubmissionResponse create(@Valid @RequestBody RateSubmissionRequest request) {
        return rateSubmissionService.create(request);
    }

    @GetMapping("/{id}")
    public RateSubmissionResponse getById(@PathVariable Long id) {
        return rateSubmissionService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        rateSubmissionService.delete(id);
    }
}
