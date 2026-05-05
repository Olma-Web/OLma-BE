package com.olma.controller;

import com.olma.dto.SubmissionTimelineItem;
import com.olma.dto.UserProfileResponse;
import com.olma.dto.UserProfileUpdateRequest;
import com.olma.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

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
}
