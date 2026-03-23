CREATE TABLE rate_submissions (
    id                   BIGSERIAL PRIMARY KEY,
    job_category_id      BIGINT NOT NULL REFERENCES job_categories(id),
    work_type_id         BIGINT NOT NULL REFERENCES work_types(id),
    region_id            BIGINT REFERENCES regions(id),
    experience_level_id  BIGINT NOT NULL REFERENCES experience_levels(id),
    submission_type      VARCHAR(10) NOT NULL,
    is_remote            BOOLEAN NOT NULL,
    complexity           VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    duration_months      NUMERIC(4,1) NOT NULL,
    amount               INTEGER NOT NULL,
    amount_unit          VARCHAR(10) NOT NULL,
    normalized_monthly   INTEGER,
    is_seed              BOOLEAN NOT NULL DEFAULT false,
    is_outlier           BOOLEAN NOT NULL DEFAULT false,
    status               VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    session_id           UUID NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_submissions_distribution
    ON rate_submissions (job_category_id, work_type_id, experience_level_id, is_remote, complexity)
    WHERE status = 'ACTIVE' AND is_outlier = false;
