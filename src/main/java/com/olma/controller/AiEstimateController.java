package com.olma.controller;

import com.olma.dto.AiEstimateCalculateRequest;
import com.olma.dto.AiEstimateResponse;
import com.olma.service.AiEstimateService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/estimates/ai")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class AiEstimateController {

    private final AiEstimateService aiEstimateService;

    @PostMapping("/calculate")
    public AiEstimateResponse calculate(@Valid @RequestBody AiEstimateCalculateRequest request) {
        return aiEstimateService.calculate(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AiEstimateResponse save(@Valid @RequestBody AiEstimateCalculateRequest request,
                                   HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return aiEstimateService.calculateAndSave(userId, request);
    }

    @GetMapping
    public List<AiEstimateResponse> getMyAiEstimates(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return aiEstimateService.getMyAiEstimates(userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        aiEstimateService.deleteAiEstimate(userId, id);
    }
}
