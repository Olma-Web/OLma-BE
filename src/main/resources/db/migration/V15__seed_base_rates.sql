-- F8 스마트 견적 계산기 베이스 단가 매트릭스 (1화면당 단가, 원 단위)
INSERT INTO base_rates (experience_level_id, job_category_id, amount)
SELECT el.id, jc.id, rates.amount
FROM (VALUES
    (1, 'web-uiux',  400000),
    (2, 'web-uiux',  600000),
    (3, 'web-uiux',  900000),
    (4, 'web-uiux', 1200000),
    (5, 'web-uiux', 1500000),
    (1, 'app-uiux',  500000),
    (2, 'app-uiux',  700000),
    (3, 'app-uiux', 1000000),
    (4, 'app-uiux', 1300000),
    (5, 'app-uiux', 1600000)
) AS rates(exp_order, slug, amount)
JOIN experience_levels el ON el.display_order = rates.exp_order
JOIN job_categories jc ON jc.slug = rates.slug
ON CONFLICT DO NOTHING;
