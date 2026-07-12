ALTER TABLE saved_estimates
    ADD COLUMN negotiation_simulation_status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    ADD COLUMN negotiation_simulation_state JSONB NOT NULL DEFAULT '{}'::jsonb,
    ADD COLUMN negotiation_simulation_started_at TIMESTAMPTZ,
    ADD COLUMN negotiation_simulation_updated_at TIMESTAMPTZ,
    ADD COLUMN negotiation_simulation_completed_at TIMESTAMPTZ;

CREATE INDEX idx_saved_estimates_user_negotiation_status_created
    ON saved_estimates (user_id, negotiation_simulation_status, created_at DESC);
