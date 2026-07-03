package com.olma.controller;

import com.olma.domain.enums.CommunityCategory;
import com.olma.dto.*;
import com.olma.service.CommunityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/community")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping("/posts")
    public CommunityPostListResponse getPosts(@RequestParam(required = false) CommunityCategory category,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return communityService.getPosts(category, page, size);
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityPostDetailResponse createPost(@Valid @RequestBody CommunityPostCreateRequest request,
                                                  HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return communityService.createPost(userId, request);
    }

    @GetMapping("/posts/{postId}")
    public CommunityPostDetailResponse getPost(@PathVariable Long postId,
                                               HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return communityService.getPost(postId, userId);
    }

    @PutMapping("/posts/{postId}")
    public CommunityPostDetailResponse updatePost(@PathVariable Long postId,
                                                  @Valid @RequestBody CommunityPostUpdateRequest request,
                                                  HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return communityService.updatePost(postId, userId, request);
    }

    @DeleteMapping("/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long postId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        communityService.deletePost(postId, userId);
    }

    @PostMapping("/posts/{postId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likePost(@PathVariable Long postId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        communityService.likePost(postId, userId);
    }

    @DeleteMapping("/posts/{postId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikePost(@PathVariable Long postId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        communityService.unlikePost(postId, userId);
    }

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityCommentResponse createComment(@PathVariable Long postId,
                                                  @Valid @RequestBody CommunityCommentCreateRequest request,
                                                  HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return communityService.createComment(postId, userId, request);
    }

    @PutMapping("/comments/{commentId}")
    public CommunityCommentResponse updateComment(@PathVariable Long commentId,
                                                  @Valid @RequestBody CommunityCommentUpdateRequest request,
                                                  HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return communityService.updateComment(commentId, userId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        communityService.deleteComment(commentId, userId);
    }

    @PostMapping("/posts/{postId}/reports")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reportPost(@PathVariable Long postId,
                           @Valid @RequestBody CommunityReportRequest request,
                           HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        communityService.reportPost(postId, userId, request);
    }

    @PostMapping("/comments/{commentId}/reports")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reportComment(@PathVariable Long commentId,
                              @Valid @RequestBody CommunityReportRequest request,
                              HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        communityService.reportComment(commentId, userId, request);
    }
}
