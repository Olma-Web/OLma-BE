package com.olma.controller;

import com.olma.domain.enums.NegotiationSimulationStatus;
import com.olma.dto.EstimateCalculateRequest;
import com.olma.dto.EstimateCalculateResponse;
import com.olma.dto.EstimateNegotiationRequest;
import com.olma.dto.NegotiationSimulationProgressRequest;
import com.olma.dto.ProjectNameUpdateRequest;
import com.olma.dto.SavedEstimateResponse;
import com.olma.domain.value.EstimateNegotiationResult;
import com.olma.service.EstimateNegotiationService;
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
    private final EstimateNegotiationService estimateNegotiationService;

    @PostMapping("/calculate")
    public EstimateCalculateResponse calculate(@Valid @RequestBody EstimateCalculateRequest request) {
        return estimateService.calculate(request);
    }

    @PostMapping("/negotiation/simulate")
    public EstimateNegotiationResult simulateNegotiation(@Valid @RequestBody EstimateNegotiationRequest request) {
        return estimateNegotiationService.simulate(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EstimateCalculateResponse save(@Valid @RequestBody EstimateCalculateRequest request,
                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return estimateService.calculateAndSave(userId, request);
    }

    @GetMapping
    public List<SavedEstimateResponse> getMyEstimates(
            @RequestParam(required = false) NegotiationSimulationStatus negotiationSimulationStatus,
            @RequestParam(required = false) Boolean negotiationSimulationStarted,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return estimateService.getMyEstimates(userId, negotiationSimulationStatus, negotiationSimulationStarted);
    }

    @GetMapping("/{id}")
    public SavedEstimateResponse getMyEstimate(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return estimateService.getMyEstimate(userId, id);
    }

    @PatchMapping("/{id}/project-name")
    public SavedEstimateResponse updateProjectName(@PathVariable Long id,
                                                   @Valid @RequestBody ProjectNameUpdateRequest request,
                                                   HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return estimateService.updateProjectName(userId, id, request);
    }

    @PatchMapping("/{id}/negotiation-simulation/start")
    public SavedEstimateResponse markNegotiationSimulationStarted(@PathVariable Long id,
                                                                  HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return estimateService.markNegotiationSimulationStarted(userId, id);
    }

    @PatchMapping("/{id}/negotiation-simulation/progress")
    public SavedEstimateResponse updateNegotiationSimulationProgress(
            @PathVariable Long id,
            @Valid @RequestBody NegotiationSimulationProgressRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return estimateService.updateNegotiationSimulationProgress(userId, id, request);
    }

    @PatchMapping("/{id}/negotiation-simulation/complete")
    public SavedEstimateResponse completeNegotiationSimulation(
            @PathVariable Long id,
            @RequestBody(required = false) NegotiationSimulationProgressRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return estimateService.completeNegotiationSimulation(userId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        estimateService.deleteEstimate(userId, id);
    }
}
