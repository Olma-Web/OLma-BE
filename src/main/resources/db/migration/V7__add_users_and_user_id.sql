CREATE TABLE users (
    id       BIGSERIAL    PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50)  NOT NULL
);

ALTER TABLE rate_submissions
    ADD COLUMN user_id BIGINT REFERENCES users(id);
