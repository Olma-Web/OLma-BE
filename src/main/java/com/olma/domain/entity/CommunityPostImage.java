package com.olma.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_post_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @Column(nullable = false, length = 2048)
    private String imageUrl;

    @Column(nullable = false)
    private Integer sortOrder;

    @Builder
    public CommunityPostImage(CommunityPost post, String imageUrl, Integer sortOrder) {
        this.post = post;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }
}
