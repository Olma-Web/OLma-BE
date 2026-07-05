package com.olma.domain.entity;

import com.olma.domain.enums.CommunityCategory;
import com.olma.domain.enums.CommunityContentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "community_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityCategory category;

    @Column(nullable = false, length = 120)
    private String title;

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
    private Integer commentCount = 0;

    @Column(nullable = false)
    private Integer reportCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CommunityContentStatus status = CommunityContentStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<CommunityPostImage> images = new ArrayList<>();

    @Builder
    public CommunityPost(User author, CommunityCategory category, String title, String content) {
        this.author = author;
        this.category = category;
        this.title = title;
        this.content = content;
        snapshotAuthorProfile(author);
    }

    public void addImage(String imageUrl, int sortOrder) {
        images.add(CommunityPostImage.builder()
                .post(this)
                .imageUrl(imageUrl)
                .sortOrder(sortOrder)
                .build());
    }

    public void update(CommunityCategory category, String title, String content, List<String> imageUrls) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.images.clear();
        for (int i = 0; i < imageUrls.size(); i++) {
            addImage(imageUrls.get(i), i);
        }
        this.updatedAt = OffsetDateTime.now();
    }

    public void increaseLikeCount() {
        this.likeCount += 1;
    }

    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

    public void increaseCommentCount() {
        this.commentCount += 1;
    }

    public void decreaseCommentCount(int count) {
        this.commentCount = Math.max(0, this.commentCount - count);
    }

    public void increaseReportCount() {
        this.reportCount += 1;
    }

    public void hide() {
        this.status = CommunityContentStatus.HIDDEN;
        this.updatedAt = OffsetDateTime.now();
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
