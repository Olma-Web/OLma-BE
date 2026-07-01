CREATE TABLE community_posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category VARCHAR(20) NOT NULL CHECK (category IN ('QNA', 'INFO', 'FREE')),
    title VARCHAR(120) NOT NULL,
    content TEXT NOT NULL,
    like_count INTEGER NOT NULL DEFAULT 0,
    comment_count INTEGER NOT NULL DEFAULT 0,
    report_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'HIDDEN')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX community_posts_status_category_created_idx
    ON community_posts (status, category, created_at DESC);

CREATE INDEX community_posts_weekly_best_idx
    ON community_posts (status, created_at DESC, like_count DESC);

CREATE TABLE community_post_images (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES community_posts(id) ON DELETE CASCADE,
    image_url VARCHAR(2048) NOT NULL,
    sort_order INTEGER NOT NULL CHECK (sort_order BETWEEN 0 AND 2),
    CONSTRAINT community_post_images_post_sort_unique UNIQUE (post_id, sort_order)
);

CREATE TABLE community_comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES community_posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_comment_id BIGINT REFERENCES community_comments(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    report_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'HIDDEN')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX community_comments_post_created_idx
    ON community_comments (post_id, created_at ASC);

CREATE TABLE community_post_likes (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES community_posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT community_post_likes_post_user_unique UNIQUE (post_id, user_id)
);

CREATE TABLE community_reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id BIGINT REFERENCES community_posts(id) ON DELETE CASCADE,
    comment_id BIGINT REFERENCES community_comments(id) ON DELETE CASCADE,
    reason VARCHAR(20) NOT NULL CHECK (reason IN ('ABUSE', 'FALSE_INFO', 'SPAM', 'ETC')),
    detail TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT community_reports_single_target CHECK (
        (post_id IS NOT NULL AND comment_id IS NULL)
        OR (post_id IS NULL AND comment_id IS NOT NULL)
    )
);

CREATE UNIQUE INDEX community_reports_reporter_post_unique
    ON community_reports (reporter_id, post_id)
    WHERE post_id IS NOT NULL;

CREATE UNIQUE INDEX community_reports_reporter_comment_unique
    ON community_reports (reporter_id, comment_id)
    WHERE comment_id IS NOT NULL;
