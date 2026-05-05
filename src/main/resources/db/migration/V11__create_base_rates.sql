CREATE TABLE base_rates (
    experience_level_id BIGINT NOT NULL REFERENCES experience_levels(id),
    job_category_id     BIGINT NOT NULL REFERENCES job_categories(id),
    amount              INTEGER NOT NULL,
    PRIMARY KEY (experience_level_id, job_category_id)
);
