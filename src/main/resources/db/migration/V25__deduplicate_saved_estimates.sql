WITH ranked_saved_estimates AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY
                user_id,
                experience_level_id,
                job_category_id,
                screen_count,
                ux_engagement,
                platform_environment,
                addon_percent,
                final_amount,
                addons ? 'DESIGN_SYSTEM',
                addons ? 'PROTOTYPING',
                addons ? 'SOURCE_TRANSFER'
            ORDER BY
                CASE negotiation_simulation_status
                    WHEN 'COMPLETED' THEN 2
                    WHEN 'IN_PROGRESS' THEN 1
                    ELSE 0
                END DESC,
                created_at DESC,
                id DESC
        ) AS duplicate_order
    FROM saved_estimates
)
DELETE FROM saved_estimates
WHERE id IN (
    SELECT id
    FROM ranked_saved_estimates
    WHERE duplicate_order > 1
);

CREATE INDEX idx_saved_estimates_user_estimate_fingerprint
    ON saved_estimates (
        user_id,
        experience_level_id,
        job_category_id,
        screen_count,
        ux_engagement,
        platform_environment,
        addon_percent,
        final_amount
    );
