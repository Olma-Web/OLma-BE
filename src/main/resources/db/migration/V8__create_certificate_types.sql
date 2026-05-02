CREATE TABLE certificate_types (
    id   BIGSERIAL   PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO certificate_types (name) VALUES
    ('웹디자인기능사'),
    ('시각디자인산업기사'),
    ('기타');

CREATE TABLE user_certificates (
    user_id             BIGINT NOT NULL REFERENCES users(id),
    certificate_type_id BIGINT NOT NULL REFERENCES certificate_types(id),
    PRIMARY KEY (user_id, certificate_type_id)
);
