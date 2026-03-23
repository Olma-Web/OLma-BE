-- 직군 (대분류)
INSERT INTO job_categories (name, slug, depth, display_order) VALUES
    ('IT/개발', 'it-dev', 0, 1),
    ('디자인', 'design', 0, 2),
    ('기획/PM', 'planning-pm', 0, 3),
    ('마케팅', 'marketing', 0, 4),
    ('영상/미디어', 'video-media', 0, 5),
    ('번역/통역', 'translation', 0, 6);

-- IT/개발 소분류
INSERT INTO job_categories (parent_id, name, slug, depth, display_order) VALUES
    (1, '백엔드', 'backend', 1, 1),
    (1, '프론트엔드', 'frontend', 1, 2),
    (1, '풀스택', 'fullstack', 1, 3),
    (1, '모바일(iOS)', 'mobile-ios', 1, 4),
    (1, '모바일(Android)', 'mobile-android', 1, 5),
    (1, '데이터 엔지니어링', 'data-engineering', 1, 6),
    (1, 'DevOps/인프라', 'devops', 1, 7);

-- 디자인 소분류
INSERT INTO job_categories (parent_id, name, slug, depth, display_order) VALUES
    (2, 'UI/UX', 'ui-ux', 1, 1),
    (2, '그래픽 디자인', 'graphic-design', 1, 2),
    (2, '브랜드 디자인', 'brand-design', 1, 3),
    (2, '3D/모션', '3d-motion', 1, 4);

-- 기획/PM 소분류
INSERT INTO job_categories (parent_id, name, slug, depth, display_order) VALUES
    (3, '서비스 기획', 'service-planning', 1, 1),
    (3, 'PM/PO', 'pm-po', 1, 2),
    (3, '사업 기획', 'business-planning', 1, 3);

-- 마케팅 소분류
INSERT INTO job_categories (parent_id, name, slug, depth, display_order) VALUES
    (4, '퍼포먼스 마케팅', 'performance-marketing', 1, 1),
    (4, '콘텐츠 마케팅', 'content-marketing', 1, 2),
    (4, 'SNS 마케팅', 'sns-marketing', 1, 3);

-- 영상/미디어 소분류
INSERT INTO job_categories (parent_id, name, slug, depth, display_order) VALUES
    (5, '영상 편집', 'video-editing', 1, 1),
    (5, '모션 그래픽', 'motion-graphics', 1, 2);

-- 번역/통역 소분류
INSERT INTO job_categories (parent_id, name, slug, depth, display_order) VALUES
    (6, '영한 번역', 'en-ko-translation', 1, 1),
    (6, '일한 번역', 'ja-ko-translation', 1, 2);

-- 업무유형
INSERT INTO work_types (name, display_order) VALUES
    ('신규 개발', 1),
    ('유지보수', 2),
    ('리디자인/리뉴얼', 3),
    ('컨설팅', 4),
    ('기타', 5);

-- 지역
INSERT INTO regions (name, display_order) VALUES
    ('서울', 1),
    ('경기/인천', 2),
    ('부산', 3),
    ('대구', 4),
    ('대전/세종', 5),
    ('광주', 6),
    ('제주', 7),
    ('기타 지역', 8);

-- 경력 구간
INSERT INTO experience_levels (label, min_years, max_years, display_order) VALUES
    ('신입 (~1년)', 0, 1, 1),
    ('주니어 (1~3년)', 1, 3, 2),
    ('미들 (3~7년)', 3, 7, 3),
    ('시니어 (7~12년)', 7, 12, 4),
    ('리드/전문가 (12년+)', 12, 99, 5);
