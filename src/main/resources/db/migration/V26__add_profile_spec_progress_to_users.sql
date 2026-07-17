ALTER TABLE users
    ADD COLUMN profile_spec_status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    ADD COLUMN profile_spec_state JSONB NOT NULL DEFAULT '{}'::jsonb,
    ADD COLUMN profile_spec_started_at TIMESTAMPTZ,
    ADD COLUMN profile_spec_updated_at TIMESTAMPTZ,
    ADD COLUMN profile_spec_completed_at TIMESTAMPTZ;

CREATE INDEX idx_users_profile_spec_status
    ON users (profile_spec_status);
