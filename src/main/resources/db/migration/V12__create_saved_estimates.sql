CREATE TABLE saved_estimates (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    experience_level_id BIGINT NOT NULL REFERENCES experience_levels(id),
    job_category_id     BIGINT NOT NULL REFERENCES job_categories(id),
    base_amount         INTEGER NOT NULL,
    screen_count        INTEGER NOT NULL,
    ux_multiplier       NUMERIC(3,2) NOT NULL,
    platform_multiplier NUMERIC(3,2) NOT NULL,
    addons              JSONB NOT NULL DEFAULT '[]'::jsonb,
    addon_percent       INTEGER NOT NULL DEFAULT 0,
    final_amount        INTEGER NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_saved_estimates_user_created
    ON saved_estimates (user_id, created_at DESC);
