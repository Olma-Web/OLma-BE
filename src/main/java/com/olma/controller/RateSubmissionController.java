package com.olma.controller;

import com.olma.dto.ProjectNameUpdateRequest;
import com.olma.dto.RateSubmissionRequest;
import com.olma.dto.RateSubmissionResponse;
import com.olma.service.RateSubmissionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
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
    public RateSubmissionResponse create(@Valid @RequestBody RateSubmissionRequest request,
                                         HttpServletRequest httpRequest) {
        if (request.getUserId() == null) {
            request.setUserId((Long) httpRequest.getAttribute("userId"));
        }
        return rateSubmissionService.create(request);
    }

    @GetMapping("/{id}")
    public RateSubmissionResponse getById(@PathVariable Long id) {
        return rateSubmissionService.getById(id);
    }

    @PatchMapping("/{id}/project-name")
    public RateSubmissionResponse updateProjectName(@PathVariable Long id,
                                                    @Valid @RequestBody ProjectNameUpdateRequest request,
                                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return rateSubmissionService.updateProjectName(userId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        rateSubmissionService.delete(id);
    }
}
