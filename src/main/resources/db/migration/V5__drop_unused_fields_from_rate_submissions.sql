DROP INDEX IF EXISTS idx_submissions_distribution;

ALTER TABLE rate_submissions
    DROP COLUMN IF EXISTS work_type_id,
    DROP COLUMN IF EXISTS region_id,
    DROP COLUMN IF EXISTS complexity;

CREATE INDEX idx_submissions_distribution
    ON rate_submissions (job_category_id, experience_level_id, is_remote)
    WHERE status = 'ACTIVE' AND is_outlier = false;
