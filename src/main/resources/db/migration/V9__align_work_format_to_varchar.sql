DROP INDEX IF EXISTS idx_submissions_distribution;

ALTER TABLE rate_submissions ADD COLUMN work_format_new VARCHAR(10);

UPDATE rate_submissions SET work_format_new = CASE work_format::text
    WHEN '상주' THEN 'ON_SITE'
    WHEN '원격' THEN 'REMOTE'
    WHEN '혼합' THEN 'HYBRID'
END;

ALTER TABLE rate_submissions ALTER COLUMN work_format_new SET NOT NULL;
ALTER TABLE rate_submissions DROP COLUMN work_format;
ALTER TABLE rate_submissions RENAME COLUMN work_format_new TO work_format;

DROP TYPE work_format_type;

ALTER TABLE rate_submissions
    ADD CONSTRAINT chk_work_format CHECK (work_format IN ('ON_SITE', 'REMOTE', 'HYBRID'));

CREATE INDEX idx_submissions_distribution
    ON rate_submissions (job_category_id, experience_level_id, work_format)
    WHERE status = 'ACTIVE' AND is_outlier = false;
