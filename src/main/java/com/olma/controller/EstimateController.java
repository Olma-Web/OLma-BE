package com.olma.controller;

import com.olma.dto.EstimateCalculateRequest;
import com.olma.dto.EstimateCalculateResponse;
import com.olma.dto.ProjectNameUpdateRequest;
import com.olma.dto.SavedEstimateResponse;
import com.olma.service.EstimateService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/estimates")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class EstimateController {

    private final EstimateService estimateService;

    @PostMapping("/calculate")
    public EstimateCalculateResponse calculate(@Valid @RequestBody EstimateCalculateRequest request) {
        return estimateService.calculate(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EstimateCalculateResponse save(@Valid @RequestBody EstimateCalculateRequest request,
                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return estimateService.calculateAndSave(userId, request);
    }

    @GetMapping
    public List<SavedEstimateResponse> getMyEstimates(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return estimateService.getMyEstimates(userId);
    }

    @PatchMapping("/{id}/project-name")
    public SavedEstimateResponse updateProjectName(@PathVariable Long id,
                                                   @Valid @RequestBody ProjectNameUpdateRequest request,
                                                   HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return estimateService.updateProjectName(userId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        estimateService.deleteEstimate(userId, id);
    }
}
