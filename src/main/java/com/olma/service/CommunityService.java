package com.olma.service;

import com.olma.domain.entity.*;
import com.olma.domain.enums.CommunityCategory;
import com.olma.domain.enums.CommunityContentStatus;
import com.olma.domain.repository.*;
import com.olma.dto.*;
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

    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final CommunityReportRepository communityReportRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CommunityPostListResponse getPosts(CommunityCategory category, int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
        Page<CommunityPost> posts = category == null
                ? communityPostRepository.findAllByStatusOrderByCreatedAtDesc(CommunityContentStatus.ACTIVE, pageRequest)
                : communityPostRepository.findAllByStatusAndCategoryOrderByCreatedAtDesc(CommunityContentStatus.ACTIVE, category, pageRequest);

        List<CommunityPost> bestPosts = communityPostRepository.findWeeklyBest(
                CommunityContentStatus.ACTIVE,
                category,
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

    private CommunityPostSummaryResponse toSummaryResponse(CommunityPost post, boolean best) {
        return CommunityPostSummaryResponse.builder()
                .id(post.getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .author(toAuthorResponse(post.getAuthor()))
                .imageUrls(post.getImages().stream().map(CommunityPostImage::getImageUrl).toList())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .best(best)
                .createdAt(post.getCreatedAt())
                .build();
    }

    private CommunityPostDetailResponse toDetailResponse(CommunityPost post, boolean likedByMe,
                                                         List<CommunityCommentResponse> comments) {
        return CommunityPostDetailResponse.builder()
                .id(post.getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .author(toAuthorResponse(post.getAuthor()))
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
                .author(toAuthorResponse(comment.getAuthor()))
                .createdAt(comment.getCreatedAt())
                .replies(replies)
                .build();
    }

    private CommunityAuthorResponse toAuthorResponse(User user) {
        String jobCategoryName = user.getJobCategory() != null ? user.getJobCategory().getName() : "직군 미설정";
        String experienceLevelLabel = user.getExperienceLevel() != null ? user.getExperienceLevel().getLabel() : "연차 미설정";
        return CommunityAuthorResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .jobCategoryName(jobCategoryName)
                .experienceLevelLabel(experienceLevelLabel)
                .badgeLabel(user.getNickname() + " [" + jobCategoryName + " • " + experienceLevelLabel + "]")
                .build();
    }
}
