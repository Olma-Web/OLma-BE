CREATE TABLE saved_ai_estimates (
    id                     BIGSERIAL PRIMARY KEY,
    user_id                BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    project_name           VARCHAR(100) NOT NULL,
    project_description    VARCHAR(3000) NOT NULL,
    platform               VARCHAR(100) NOT NULL,
    estimated_screen_count INTEGER NOT NULL,
    features               JSONB NOT NULL DEFAULT '[]'::jsonb,
    schedule               JSONB NOT NULL,
    risks                  JSONB NOT NULL DEFAULT '[]'::jsonb,
    breakdown              JSONB NOT NULL DEFAULT '[]'::jsonb,
    total_expected_days    INTEGER NOT NULL,
    final_amount           INTEGER NOT NULL,
    client_message         TEXT NOT NULL,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_saved_ai_estimates_user_created
    ON saved_ai_estimates (user_id, created_at DESC);
