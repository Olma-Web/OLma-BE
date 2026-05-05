-- 1. Experience levels: spec labels and boundaries (ids 1..5 preserved)
UPDATE experience_levels SET label='1년 미만',           min_years=0,  max_years=1,  display_order=1 WHERE id=1;
UPDATE experience_levels SET label='1~3년 차 (주니어)',  min_years=1,  max_years=3,  display_order=2 WHERE id=2;
UPDATE experience_levels SET label='4~6년 차 (미들)',    min_years=4,  max_years=6,  display_order=3 WHERE id=3;
UPDATE experience_levels SET label='7~9년 차 (시니어)',  min_years=7,  max_years=9,  display_order=4 WHERE id=4;
UPDATE experience_levels SET label='10년 차 이상 (리더)', min_years=10, max_years=99, display_order=5 WHERE id=5;

-- 2. Replace 'ui-ux' with 'web-uiux'; insert 'app-uiux' as second design sub
UPDATE job_categories SET display_order = display_order + 1 WHERE parent_id = 2 AND slug <> 'ui-ux';
UPDATE job_categories SET name='웹 UI/UX', slug='web-uiux', display_order=1 WHERE slug='ui-ux';
INSERT INTO job_categories (parent_id, name, slug, depth, display_order)
VALUES (2, '앱 UI/UX', 'app-uiux', 1, 2);

-- 3. Certificate types: spec wording
UPDATE certificate_types SET name='시각디자인기사·산업기사' WHERE name='시각디자인산업기사';
UPDATE certificate_types SET name='기타 자격증'           WHERE name='기타';

-- 4. duration nullable (only collected for TOTAL/건별 per spec)
ALTER TABLE rate_submissions ALTER COLUMN duration DROP NOT NULL;

-- 5. Drop legacy V4 backend seed (not aligned with spec design domain)
DELETE FROM rate_submissions WHERE is_seed = true;

-- 6. 50 dummy users with random profile spec
INSERT INTO users (email, password, nickname, agreement_at, experience_level_id, job_category_id)
SELECT
    'seed' || gs || '@olma.local',
    '$$SEED$$NOT_A_REAL_HASH_DO_NOT_LOGIN',
    '디자이너' || gs,
    now() - (random() * 365 * 86400)::int * interval '1 second',
    1 + floor(random() * 5)::int,
    CASE WHEN random() < 0.5
         THEN (SELECT id FROM job_categories WHERE slug='web-uiux')
         ELSE (SELECT id FROM job_categories WHERE slug='app-uiux')
    END
FROM generate_series(1, 50) AS gs;

-- 7. ~60% of seeded users get one random certificate
INSERT INTO user_certificates (user_id, certificate_type_id)
SELECT u.id, ct.id
FROM users u
CROSS JOIN LATERAL (SELECT id FROM certificate_types ORDER BY random() LIMIT 1) ct
WHERE u.email LIKE 'seed%@olma.local'
  AND random() < 0.6;

-- 8. 700 diverse rate_submissions across web-uiux + app-uiux × 5 levels
WITH job_ids AS (
    SELECT
        (SELECT id FROM job_categories WHERE slug='web-uiux') AS web_id,
        (SELECT id FROM job_categories WHERE slug='app-uiux') AS app_id
),
seed_users AS (
    SELECT array_agg(id) AS ids FROM users WHERE email LIKE 'seed%@olma.local'
),
params AS (
    SELECT
        gs,
        CASE WHEN random() < 0.5 THEN (SELECT web_id FROM job_ids) ELSE (SELECT app_id FROM job_ids) END AS job_id,
        1 + floor(random() * 5)::int AS exp_id,
        CASE floor(random() * 3)::int
            WHEN 0 THEN 'ON_SITE'
            WHEN 1 THEN 'REMOTE'
            ELSE 'HYBRID'
        END AS wf,
        CASE WHEN random() < 0.5 THEN 'TRACK_A' ELSE 'TRACK_B' END AS st,
        CASE WHEN random() < 0.6 THEN 'MONTHLY' ELSE 'TOTAL' END AS au,
        CASE floor(random() * 5)::int
            WHEN 0 THEN '1주일 이하'
            WHEN 1 THEN '2~3주'
            WHEN 2 THEN '1개월'
            WHEN 3 THEN '2~3개월'
            ELSE '3개월 이상'
        END AS dur,
        random() AS link_user
    FROM generate_series(1, 700) AS gs
),
priced AS (
    SELECT
        p.*,
        ((CASE
            WHEN p.job_id = (SELECT web_id FROM job_ids) THEN
                CASE p.exp_id
                    WHEN 1 THEN 280
                    WHEN 2 THEN 380
                    WHEN 3 THEN 530
                    WHEN 4 THEN 720
                    WHEN 5 THEN 1050
                END
            ELSE
                CASE p.exp_id
                    WHEN 1 THEN 320
                    WHEN 2 THEN 420
                    WHEN 3 THEN 580
                    WHEN 4 THEN 800
                    WHEN 5 THEN 1150
                END
        END) * (0.75 + random() * 0.5))::int AS monthly_amount
    FROM params p
)
INSERT INTO rate_submissions (
    job_category_id, experience_level_id, user_id,
    submission_type, work_format, duration,
    amount, amount_unit, normalized_monthly,
    is_seed, is_outlier, status, session_id, created_at
)
SELECT
    p.job_id,
    p.exp_id,
    CASE WHEN p.link_user < 0.5
         THEN (SELECT ids[1 + floor(random() * array_length(ids, 1))::int] FROM seed_users)
         ELSE NULL
    END,
    p.st,
    p.wf,
    CASE WHEN p.au = 'MONTHLY' THEN NULL ELSE p.dur END,
    CASE p.au
        WHEN 'MONTHLY' THEN p.monthly_amount
        ELSE (p.monthly_amount * (
            CASE p.dur
                WHEN '1주일 이하' THEN 0.25
                WHEN '2~3주'      THEN 0.625
                WHEN '1개월'       THEN 1.0
                WHEN '2~3개월'     THEN 2.5
                ELSE 3.5
            END
        ))::int
    END,
    p.au,
    p.monthly_amount,
    true,
    false,
    'ACTIVE',
    gen_random_uuid(),
    now() - (random() * 180 * 86400)::int * interval '1 second'
FROM priced p;
