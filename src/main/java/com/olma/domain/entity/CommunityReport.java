package com.olma.domain.entity;

import com.olma.domain.enums.CommunityReportReason;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "community_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private CommunityComment comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder
    public CommunityReport(User reporter, CommunityPost post, CommunityComment comment,
                           CommunityReportReason reason, String detail) {
        this.reporter = reporter;
        this.post = post;
        this.comment = comment;
        this.reason = reason;
        this.detail = detail;
    }
}
