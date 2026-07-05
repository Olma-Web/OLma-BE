package com.olma.domain.enums;

import org.springframework.data.domain.Sort;

public enum CommunityPostSort {
    LATEST,
    LIKES,
    COMMENTS;

    public Sort toSort() {
        return switch (this) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case LIKES -> Sort.by(Sort.Direction.DESC, "likeCount")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case COMMENTS -> Sort.by(Sort.Direction.DESC, "commentCount")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        };
    }
}
