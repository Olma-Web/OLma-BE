package com.olma.service;

import com.olma.domain.entity.*;
import com.olma.domain.enums.CommunityCategory;
import com.olma.domain.enums.CommunityContentStatus;
import com.olma.domain.enums.CommunityPostSort;
import com.olma.domain.repository.*;
import com.olma.dto.*;
import com.olma.exception.ForbiddenException;
import com.olma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int WEEKLY_BEST_LIMIT = 3;
    private static final int CONTENT_PREVIEW_LENGTH = 120;

    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final CommunityCommentLikeRepository communityCommentLikeRepository;
    private final CommunityReportRepository communityReportRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CommunityPostListResponse getPosts(CommunityCategory category, CommunityPostSort sort,
                                              Long jobCategoryId, Long experienceLevelId,
                                              int page, int size) {
        CommunityPostSort resolvedSort = sort == null ? CommunityPostSort.LATEST : sort;
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                resolvedSort.toSort()
        );
        Page<CommunityPost> posts = communityPostRepository.findPosts(
                CommunityContentStatus.ACTIVE,
                category,
                jobCategoryId,
                experienceLevelId,
                pageRequest
        );

        List<CommunityPost> bestPosts = communityPostRepository.findWeeklyBest(
                CommunityContentStatus.ACTIVE,
                category,
                jobCategoryId,
                experienceLevelId,
                OffsetDateTime.now().minusDays(7),
                PageRequest.of(0, WEEKLY_BEST_LIMIT)
        );
        Set<Long> bestPostIds = new HashSet<>(bestPosts.stream().map(CommunityPost::getId).toList());

        return CommunityPostListResponse.builder()
                .bestPosts(bestPosts.stream()
                        .map(post -> toSummaryResponse(post, true))
                        .toList())
                .posts(posts.getContent().stream()
                        .map(post -> toSummaryResponse(post, bestPostIds.contains(post.getId())))
                        .toList())
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public CommunityPostPageResponse getMyPosts(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: id=" + userId);
        }
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
        Page<CommunityPost> posts = communityPostRepository.findAllByAuthor_IdAndStatusOrderByCreatedAtDesc(
                userId,
                CommunityContentStatus.ACTIVE,
                pageRequest
        );

        return CommunityPostPageResponse.builder()
                .posts(posts.getContent().stream()
                        .map(post -> toSummaryResponse(post, false))
                        .toList())
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public CommunityCommentListResponse getMyComments(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: id=" + userId);
        }
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
        Page<CommunityComment> comments = communityCommentRepository.findMyActiveComments(
                userId,
                CommunityContentStatus.ACTIVE,
                CommunityContentStatus.ACTIVE,
                pageRequest
        );

        return CommunityCommentListResponse.builder()
                .comments(comments.getContent().stream()
                        .map(this::toMyCommentResponse)
                        .toList())
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .build();
    }

    @Transactional
    public CommunityPostDetailResponse createPost(Long userId, CommunityPostCreateRequest request) {
        User author = getUser(userId);
        List<String> imageUrls = request.getImageUrls() == null ? List.of() : request.getImageUrls();
        if (imageUrls.size() > 3) {
            throw new IllegalArgumentException("이미지는 최대 3장까지 첨부할 수 있습니다.");
        }

        CommunityPost post = CommunityPost.builder()
                .author(author)
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        for (int i = 0; i < imageUrls.size(); i++) {
            post.addImage(imageUrls.get(i), i);
        }

        CommunityPost saved = communityPostRepository.save(post);
        log.info("community post created postId={} userId={}", saved.getId(), userId);
        return toDetailResponse(saved, false, List.of());
    }

    @Transactional(readOnly = true)
    public CommunityPostDetailResponse getPost(Long postId, Long userId) {
        CommunityPost post = getActivePost(postId);
        boolean likedByMe = communityPostLikeRepository.findByPost_IdAndUser_Id(postId, userId).isPresent();
        List<CommunityCommentResponse> comments = buildCommentTree(
                communityCommentRepository.findAllByPost_IdAndStatusOrderByCreatedAtAsc(postId, CommunityContentStatus.ACTIVE)
        );
        return toDetailResponse(post, likedByMe, comments);
    }

    @Transactional
    public CommunityPostDetailResponse updatePost(Long postId, Long userId, CommunityPostUpdateRequest request) {
        CommunityPost post = getActivePost(postId);
        validateAuthor(post.getAuthor().getId(), userId);
        List<String> imageUrls = request.getImageUrls() == null ? List.of() : request.getImageUrls();
        if (imageUrls.size() > 3) {
            throw new IllegalArgumentException("이미지는 최대 3장까지 첨부할 수 있습니다.");
        }

        post.update(request.getCategory(), request.getTitle(), request.getContent(), imageUrls);
        boolean likedByMe = communityPostLikeRepository.findByPost_IdAndUser_Id(postId, userId).isPresent();
        List<CommunityCommentResponse> comments = buildCommentTree(
                communityCommentRepository.findAllByPost_IdAndStatusOrderByCreatedAtAsc(postId, CommunityContentStatus.ACTIVE)
        );
        log.info("community post updated postId={} userId={}", postId, userId);
        return toDetailResponse(post, likedByMe, comments);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        CommunityPost post = getActivePost(postId);
        validateAuthor(post.getAuthor().getId(), userId);
        post.hide();
        log.info("community post deleted postId={} userId={}", postId, userId);
    }

    @Transactional
    public void likePost(Long postId, Long userId) {
        CommunityPost post = getActivePost(postId);
        User user = getUser(userId);
        if (communityPostLikeRepository.findByPost_IdAndUser_Id(postId, userId).isPresent()) {
            return;
        }
        communityPostLikeRepository.save(CommunityPostLike.builder()
                .post(post)
                .user(user)
                .build());
        post.increaseLikeCount();
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        CommunityPost post = getActivePost(postId);
        communityPostLikeRepository.findByPost_IdAndUser_Id(postId, userId).ifPresent(like -> {
            communityPostLikeRepository.delete(like);
            post.decreaseLikeCount();
        });
    }

    @Transactional
    public void likeComment(Long commentId, Long userId) {
        CommunityComment comment = getActiveComment(commentId);
        User user = getUser(userId);
        if (communityCommentLikeRepository.findByComment_IdAndUser_Id(commentId, userId).isPresent()) {
            return;
        }
        communityCommentLikeRepository.save(CommunityCommentLike.builder()
                .comment(comment)
                .user(user)
                .build());
        comment.increaseLikeCount();
    }

    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        CommunityComment comment = getActiveComment(commentId);
        communityCommentLikeRepository.findByComment_IdAndUser_Id(commentId, userId).ifPresent(like -> {
            communityCommentLikeRepository.delete(like);
            comment.decreaseLikeCount();
        });
    }

    @Transactional
    public CommunityCommentResponse createComment(Long postId, Long userId, CommunityCommentCreateRequest request) {
        CommunityPost post = getActivePost(postId);
        User author = getUser(userId);
        CommunityComment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = communityCommentRepository.findById(request.getParentCommentId())
                    .filter(comment -> comment.getStatus() == CommunityContentStatus.ACTIVE)
                    .orElseThrow(() -> new NotFoundException("Comment not found: id=" + request.getParentCommentId()));
            if (!Objects.equals(parentComment.getPost().getId(), postId)) {
                throw new IllegalArgumentException("Parent comment does not belong to this post");
            }
            if (parentComment.getParentComment() != null) {
                throw new IllegalArgumentException("대댓글에는 다시 답글을 달 수 없습니다.");
            }
        }

        CommunityComment comment = communityCommentRepository.save(CommunityComment.builder()
                .post(post)
                .author(author)
                .parentComment(parentComment)
                .content(request.getContent())
                .build());
        post.increaseCommentCount();
        log.info("community comment created commentId={} postId={} userId={}", comment.getId(), postId, userId);
        return toCommentResponse(comment, List.of());
    }

    @Transactional
    public CommunityCommentResponse updateComment(Long commentId, Long userId, CommunityCommentUpdateRequest request) {
        CommunityComment comment = getActiveComment(commentId);
        validateAuthor(comment.getAuthor().getId(), userId);
        comment.update(request.getContent());
        log.info("community comment updated commentId={} userId={}", commentId, userId);
        return toCommentResponse(comment, List.of());
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        CommunityComment comment = getActiveComment(commentId);
        validateAuthor(comment.getAuthor().getId(), userId);

        int hiddenCount = 1;
        comment.hide();
        if (comment.getParentComment() == null) {
            List<CommunityComment> replies = communityCommentRepository.findAllByParentComment_IdAndStatus(
                    commentId,
                    CommunityContentStatus.ACTIVE
            );
            replies.forEach(CommunityComment::hide);
            hiddenCount += replies.size();
        }
        comment.getPost().decreaseCommentCount(hiddenCount);
        log.info("community comment deleted commentId={} userId={} hiddenCount={}", commentId, userId, hiddenCount);
    }

    @Transactional
    public void reportPost(Long postId, Long userId, CommunityReportRequest request) {
        CommunityPost post = getActivePost(postId);
        User reporter = getUser(userId);
        if (communityReportRepository.existsByReporter_IdAndPost_Id(userId, postId)) {
            throw new IllegalArgumentException("이미 신고한 게시글입니다.");
        }
        communityReportRepository.save(CommunityReport.builder()
                .reporter(reporter)
                .post(post)
                .reason(request.getReason())
                .detail(request.getDetail())
                .build());
        post.increaseReportCount();
    }

    @Transactional
    public void reportComment(Long commentId, Long userId, CommunityReportRequest request) {
        CommunityComment comment = communityCommentRepository.findById(commentId)
                .filter(c -> c.getStatus() == CommunityContentStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Comment not found: id=" + commentId));
        User reporter = getUser(userId);
        if (communityReportRepository.existsByReporter_IdAndComment_Id(userId, commentId)) {
            throw new IllegalArgumentException("이미 신고한 댓글입니다.");
        }
        communityReportRepository.save(CommunityReport.builder()
                .reporter(reporter)
                .comment(comment)
                .reason(request.getReason())
                .detail(request.getDetail())
                .build());
        comment.increaseReportCount();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
    }

    private CommunityPost getActivePost(Long postId) {
        return communityPostRepository.findActiveByIdWithAuthor(postId, CommunityContentStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Post not found: id=" + postId));
    }

    private CommunityComment getActiveComment(Long commentId) {
        return communityCommentRepository.findById(commentId)
                .filter(comment -> comment.getStatus() == CommunityContentStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Comment not found: id=" + commentId));
    }

    private void validateAuthor(Long authorId, Long userId) {
        if (!Objects.equals(authorId, userId)) {
            throw new ForbiddenException("작성자만 수정하거나 삭제할 수 있습니다.");
        }
    }

    private CommunityPostSummaryResponse toSummaryResponse(CommunityPost post, boolean best) {
        return CommunityPostSummaryResponse.builder()
                .id(post.getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .contentPreview(toContentPreview(post.getContent()))
                .author(toAuthorResponse(
                        post.getAuthor(),
                        post.getAuthorJobCategoryId(),
                        post.getAuthorJobCategoryName(),
                        post.getAuthorExperienceLevelId(),
                        post.getAuthorExperienceLevelLabel()
                ))
                .imageUrls(post.getImages().stream().map(CommunityPostImage::getImageUrl).toList())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .best(best)
                .createdAt(post.getCreatedAt())
                .build();
    }

    private String toContentPreview(String content) {
        String normalized = content.strip().replaceAll("\\s+", " ");
        if (normalized.codePointCount(0, normalized.length()) <= CONTENT_PREVIEW_LENGTH) {
            return normalized;
        }
        int endIndex = normalized.offsetByCodePoints(0, CONTENT_PREVIEW_LENGTH);
        return normalized.substring(0, endIndex);
    }

    private CommunityPostDetailResponse toDetailResponse(CommunityPost post, boolean likedByMe,
                                                         List<CommunityCommentResponse> comments) {
        return CommunityPostDetailResponse.builder()
                .id(post.getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .author(toAuthorResponse(
                        post.getAuthor(),
                        post.getAuthorJobCategoryId(),
                        post.getAuthorJobCategoryName(),
                        post.getAuthorExperienceLevelId(),
                        post.getAuthorExperienceLevelLabel()
                ))
                .imageUrls(post.getImages().stream().map(CommunityPostImage::getImageUrl).toList())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .likedByMe(likedByMe)
                .createdAt(post.getCreatedAt())
                .comments(comments)
                .build();
    }

    private List<CommunityCommentResponse> buildCommentTree(List<CommunityComment> comments) {
        Map<Long, List<CommunityComment>> repliesByParentId = new LinkedHashMap<>();
        List<CommunityComment> roots = new ArrayList<>();
        for (CommunityComment comment : comments) {
            if (comment.getParentComment() == null) {
                roots.add(comment);
            } else {
                repliesByParentId
                        .computeIfAbsent(comment.getParentComment().getId(), key -> new ArrayList<>())
                        .add(comment);
            }
        }
        return roots.stream()
                .map(comment -> toCommentResponse(comment, repliesByParentId
                        .getOrDefault(comment.getId(), List.of())
                        .stream()
                        .map(reply -> toCommentResponse(reply, List.of()))
                        .toList()))
                .toList();
    }

    private CommunityCommentResponse toCommentResponse(CommunityComment comment, List<CommunityCommentResponse> replies) {
        return CommunityCommentResponse.builder()
                .id(comment.getId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .content(comment.getContent())
                .author(toAuthorResponse(
                        comment.getAuthor(),
                        comment.getAuthorJobCategoryId(),
                        comment.getAuthorJobCategoryName(),
                        comment.getAuthorExperienceLevelId(),
                        comment.getAuthorExperienceLevelLabel()
                ))
                .createdAt(comment.getCreatedAt())
                .replies(replies)
                .build();
    }

    private CommunityMyCommentResponse toMyCommentResponse(CommunityComment comment) {
        CommunityPost post = comment.getPost();
        return CommunityMyCommentResponse.builder()
                .id(comment.getId())
                .postId(post.getId())
                .postTitle(post.getTitle())
                .postCategory(post.getCategory())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private CommunityAuthorResponse toAuthorResponse(User user, Long jobCategoryId, String jobCategoryName,
                                                     Long experienceLevelId, String experienceLevelLabel) {
        String resolvedJobCategoryName = jobCategoryName != null ? jobCategoryName : "직군 미설정";
        String resolvedExperienceLevelLabel = experienceLevelLabel != null ? experienceLevelLabel : "연차 미설정";
        return CommunityAuthorResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .jobCategoryId(jobCategoryId)
                .jobCategoryName(resolvedJobCategoryName)
                .experienceLevelId(experienceLevelId)
                .experienceLevelLabel(resolvedExperienceLevelLabel)
                .badgeLabel(user.getNickname() + " [" + resolvedJobCategoryName + " • " + resolvedExperienceLevelLabel + "]")
                .build();
    }
}
