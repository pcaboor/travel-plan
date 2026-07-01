-- Traveler feedback on travels they participated in. Feeds the manager
-- performance score (V2-5) and the Neo4j recommendations (V2-7).
CREATE TABLE feedback (
    id              UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    author_user_id  UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    travel_ref_id   UUID          NOT NULL,
    rating          INT           NOT NULL,
    comment         VARCHAR(2000),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT feedback_rating_check     CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_feedback_author_travel UNIQUE (author_user_id, travel_ref_id)
);

CREATE INDEX idx_feedback_travel ON feedback (travel_ref_id);
CREATE INDEX idx_feedback_author ON feedback (author_user_id);
