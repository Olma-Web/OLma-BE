CREATE INDEX community_posts_user_status_created_idx
    ON community_posts (user_id, status, created_at DESC);

CREATE INDEX community_comments_user_status_created_idx
    ON community_comments (user_id, status, created_at DESC);
