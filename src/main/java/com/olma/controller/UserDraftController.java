package com.olma.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.olma.domain.enums.UserDraftType;
import com.olma.dto.UserDraftRequest;
import com.olma.dto.UserDraftResponse;
import com.olma.service.UserDraftService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users/me/drafts")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class UserDraftController {

    private final UserDraftService userDraftService;

    @GetMapping
    public List<UserDraftResponse> getDrafts(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return userDraftService.getDrafts(userId);
    }

    @GetMapping("/{type}")
    public UserDraftResponse getDraft(@PathVariable UserDraftType type,
                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return userDraftService.getDraft(userId, type);
    }

    @PatchMapping("/{type}")
    public UserDraftResponse saveProgress(@PathVariable UserDraftType type,
                                          @Valid @RequestBody UserDraftRequest request,
                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return userDraftService.saveProgress(userId, type, request.getState());
    }

    @PatchMapping("/{type}/complete")
    public UserDraftResponse complete(@PathVariable UserDraftType type,
                                      @RequestBody(required = false) UserDraftRequest request,
                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        JsonNode state = request != null ? request.getState() : null;
        return userDraftService.complete(userId, type, state);
    }

    @DeleteMapping("/{type}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDraft(@PathVariable UserDraftType type,
                            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userDraftService.deleteDraft(userId, type);
    }
}
