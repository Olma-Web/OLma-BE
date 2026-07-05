ALTER TABLE community_posts
    ADD COLUMN author_job_category_id BIGINT,
    ADD COLUMN author_job_category_name VARCHAR(255),
    ADD COLUMN author_experience_level_id BIGINT,
    ADD COLUMN author_experience_level_label VARCHAR(255);

ALTER TABLE community_comments
    ADD COLUMN author_job_category_id BIGINT,
    ADD COLUMN author_job_category_name VARCHAR(255),
    ADD COLUMN author_experience_level_id BIGINT,
    ADD COLUMN author_experience_level_label VARCHAR(255);

UPDATE community_posts p
SET author_job_category_id = u.job_category_id,
    author_job_category_name = jc.name,
    author_experience_level_id = u.experience_level_id,
    author_experience_level_label = el.label
FROM users u
LEFT JOIN job_categories jc ON jc.id = u.job_category_id
LEFT JOIN experience_levels el ON el.id = u.experience_level_id
WHERE p.user_id = u.id;

UPDATE community_comments c
SET author_job_category_id = u.job_category_id,
    author_job_category_name = jc.name,
    author_experience_level_id = u.experience_level_id,
    author_experience_level_label = el.label
FROM users u
LEFT JOIN job_categories jc ON jc.id = u.job_category_id
LEFT JOIN experience_levels el ON el.id = u.experience_level_id
WHERE c.user_id = u.id;

CREATE INDEX community_posts_filter_latest_idx
    ON community_posts (status, category, author_job_category_id, author_experience_level_id, created_at DESC);

CREATE INDEX community_posts_filter_likes_idx
    ON community_posts (status, category, author_job_category_id, author_experience_level_id, like_count DESC, created_at DESC);

CREATE INDEX community_posts_filter_comments_idx
    ON community_posts (status, category, author_job_category_id, author_experience_level_id, comment_count DESC, created_at DESC);
