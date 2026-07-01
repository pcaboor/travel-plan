-- Reports filed by travelers against a manager, another traveler, or a travel.
-- Admins review them and move them through their lifecycle.
CREATE TABLE reports (
    id                UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    reporter_user_id  UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    target_type       VARCHAR(32)   NOT NULL,
    target_id         UUID          NOT NULL,
    reason            VARCHAR(2000) NOT NULL,
    status            VARCHAR(32)   NOT NULL DEFAULT 'OPEN',
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT reports_target_type_check CHECK (target_type IN ('MANAGER', 'TRAVELER', 'TRAVEL')),
    CONSTRAINT reports_status_check      CHECK (status IN ('OPEN', 'REVIEWED', 'DISMISSED', 'ACTIONED'))
);

CREATE INDEX idx_reports_reporter ON reports (reporter_user_id);
CREATE INDEX idx_reports_status   ON reports (status);
CREATE INDEX idx_reports_target   ON reports (target_type, target_id);
