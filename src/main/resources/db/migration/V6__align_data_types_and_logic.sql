-- 1. is_remote (BOOLEAN) → work_format (ENUM: 상주 / 원격 / 혼합)
CREATE TYPE work_format_type AS ENUM ('상주', '원격', '혼합');

DROP INDEX IF EXISTS idx_submissions_distribution;

ALTER TABLE rate_submissions
    ADD COLUMN work_format work_format_type;

UPDATE rate_submissions
    SET work_format = CASE WHEN is_remote THEN '원격'::work_format_type ELSE '상주'::work_format_type END;

ALTER TABLE rate_submissions
    ALTER COLUMN work_format SET NOT NULL,
    DROP COLUMN is_remote;

-- 2. duration_months (NUMERIC) → duration (VARCHAR) to support ranges like "2~3 weeks"
ALTER TABLE rate_submissions
    ADD COLUMN duration VARCHAR(50);

UPDATE rate_submissions
    SET duration = duration_months::text;

ALTER TABLE rate_submissions
    DROP COLUMN duration_months;

-- 3. submission_type: ESTIMATE/COMPARE → TRACK_A/TRACK_B
UPDATE rate_submissions SET submission_type = 'TRACK_A' WHERE submission_type = 'ESTIMATE';
UPDATE rate_submissions SET submission_type = 'TRACK_B' WHERE submission_type = 'COMPARE';

-- Recreate index
CREATE INDEX idx_submissions_distribution
    ON rate_submissions (job_category_id, experience_level_id, work_format)
    WHERE status = 'ACTIVE' AND is_outlier = false;
