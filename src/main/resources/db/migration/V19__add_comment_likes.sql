ALTER TABLE community_comments ADD COLUMN like_count INTEGER NOT NULL DEFAULT 0;

CREATE TABLE community_comment_likes (
    id         BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL REFERENCES community_comments(id),
    user_id    BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT community_comment_likes_comment_user_unique UNIQUE (comment_id, user_id)
);
