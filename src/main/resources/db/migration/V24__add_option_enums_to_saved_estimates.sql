ALTER TABLE saved_estimates
    ADD COLUMN ux_engagement VARCHAR(20),
    ADD COLUMN platform_environment VARCHAR(20);

UPDATE saved_estimates
SET ux_engagement = CASE ux_multiplier
        WHEN 1.0 THEN 'GUI_ONLY'
        WHEN 1.3 THEN 'WIREFRAME_PLUS'
        WHEN 1.8 THEN 'FULL_PLANNING'
    END,
    -- platform_multiplier 1.0 is shared by MOBILE_APP and PC_WEB and cannot be
    -- recovered from historical data; existing rows default to MOBILE_APP.
    platform_environment = CASE
        WHEN platform_multiplier = 1.5 THEN 'RESPONSIVE_WEB'
        ELSE 'MOBILE_APP'
    END;

ALTER TABLE saved_estimates
    ALTER COLUMN ux_engagement SET NOT NULL,
    ALTER COLUMN platform_environment SET NOT NULL;
