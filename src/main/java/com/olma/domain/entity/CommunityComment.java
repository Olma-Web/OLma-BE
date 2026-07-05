package com.olma.domain.entity;

import com.olma.domain.enums.CommunityContentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "community_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private CommunityComment parentComment;

    @Column(name = "author_job_category_id")
    private Long authorJobCategoryId;

    @Column(name = "author_job_category_name")
    private String authorJobCategoryName;

    @Column(name = "author_experience_level_id")
    private Long authorExperienceLevelId;

    @Column(name = "author_experience_level_label")
    private String authorExperienceLevelLabel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer likeCount = 0;

    @Column(nullable = false)
    private Integer reportCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CommunityContentStatus status = CommunityContentStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder
    public CommunityComment(CommunityPost post, User author, CommunityComment parentComment, String content) {
        this.post = post;
        this.author = author;
        this.parentComment = parentComment;
        this.content = content;
        snapshotAuthorProfile(author);
    }

    public void increaseLikeCount() {
        this.likeCount += 1;
    }

    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

    public void increaseReportCount() {
        this.reportCount += 1;
    }

    public void update(String content) {
        this.content = content;
    }

    public void hide() {
        this.status = CommunityContentStatus.HIDDEN;
    }

    private void snapshotAuthorProfile(User author) {
        if (author.getJobCategory() != null) {
            this.authorJobCategoryId = author.getJobCategory().getId();
            this.authorJobCategoryName = author.getJobCategory().getName();
        }
        if (author.getExperienceLevel() != null) {
            this.authorExperienceLevelId = author.getExperienceLevel().getId();
            this.authorExperienceLevelLabel = author.getExperienceLevel().getLabel();
        }
    }
}
