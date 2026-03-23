CREATE TABLE job_categories (
    id         BIGSERIAL PRIMARY KEY,
    parent_id  BIGINT REFERENCES job_categories(id),
    name       VARCHAR(100) NOT NULL,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    depth      SMALLINT NOT NULL DEFAULT 0,
    is_active  BOOLEAN NOT NULL DEFAULT true,
    display_order SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_job_categories_parent ON job_categories(parent_id) WHERE parent_id IS NOT NULL;

CREATE TABLE work_types (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    display_order SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE regions (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    display_order SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE experience_levels (
    id         BIGSERIAL PRIMARY KEY,
    label      VARCHAR(50) NOT NULL,
    min_years  SMALLINT NOT NULL,
    max_years  SMALLINT NOT NULL,
    display_order SMALLINT NOT NULL DEFAULT 0
);
