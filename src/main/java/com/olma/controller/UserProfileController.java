package com.olma.controller;

import com.olma.dto.ChangePasswordRequest;
import com.olma.dto.SubmissionTimelineItem;
import com.olma.dto.UserProfileResponse;
import com.olma.dto.UserProfileUpdateRequest;
import com.olma.service.AuthService;
import com.olma.service.UserProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final AuthService authService;

    @GetMapping("/{userId}")
    public UserProfileResponse getProfile(@PathVariable Long userId) {
        return userProfileService.getProfile(userId);
    }

    @PutMapping("/{userId}/profile")
    public UserProfileResponse updateProfile(@PathVariable Long userId,
                                             @RequestBody UserProfileUpdateRequest request) {
        return userProfileService.updateProfile(userId, request);
    }

    @GetMapping("/{userId}/submissions")
    public List<SubmissionTimelineItem> getSubmissions(@PathVariable Long userId) {
        return userProfileService.getSubmissions(userId);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request,
                               HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userProfileService.changePassword(userId, request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        authService.withdraw(userId);
    }
}
