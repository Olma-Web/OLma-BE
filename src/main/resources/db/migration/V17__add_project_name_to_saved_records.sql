ALTER TABLE saved_estimates
    ADD COLUMN project_name VARCHAR(100);

UPDATE saved_estimates
SET project_name = TO_CHAR(created_at AT TIME ZONE 'Asia/Seoul', 'YY.MM.DD') || ' 견적서'
WHERE project_name IS NULL;

ALTER TABLE saved_estimates
    ALTER COLUMN project_name SET NOT NULL;

ALTER TABLE rate_submissions
    ADD COLUMN project_name VARCHAR(100);

UPDATE rate_submissions
SET project_name = TO_CHAR(created_at AT TIME ZONE 'Asia/Seoul', 'YY.MM.DD') || ' 단가 기록'
WHERE project_name IS NULL;

ALTER TABLE rate_submissions
    ALTER COLUMN project_name SET NOT NULL;
