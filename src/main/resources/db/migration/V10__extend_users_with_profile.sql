ALTER TABLE users
    ADD COLUMN email VARCHAR(255),
    ADD COLUMN agreement_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN experience_level_id BIGINT REFERENCES experience_levels(id),
    ADD COLUMN job_category_id BIGINT REFERENCES job_categories(id);

UPDATE users SET email = id::text || '@placeholder.local' WHERE email IS NULL;

ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT users_email_unique UNIQUE (email);

ALTER TABLE rate_submissions
    DROP CONSTRAINT IF EXISTS rate_submissions_user_id_fkey;
ALTER TABLE rate_submissions
    ADD CONSTRAINT rate_submissions_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_certificates
    DROP CONSTRAINT IF EXISTS user_certificates_user_id_fkey;
ALTER TABLE user_certificates
    ADD CONSTRAINT user_certificates_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
