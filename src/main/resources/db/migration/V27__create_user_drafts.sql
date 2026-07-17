CREATE TABLE user_drafts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    draft_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    state JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT uk_user_drafts_user_type UNIQUE (user_id, draft_type)
);

CREATE INDEX idx_user_drafts_user_id
    ON user_drafts (user_id);

CREATE INDEX idx_user_drafts_user_status
    ON user_drafts (user_id, status);
