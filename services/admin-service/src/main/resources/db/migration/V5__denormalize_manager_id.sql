-- Denormalize the owning manager onto bookings and feedback so per-manager
-- statistics (income, trips, ratings) and the manager leaderboard can be
-- computed locally in admin-service, without a cross-service call.
ALTER TABLE bookings ADD COLUMN manager_id UUID;
ALTER TABLE feedback ADD COLUMN manager_id UUID;

CREATE INDEX idx_bookings_manager ON bookings (manager_id);
CREATE INDEX idx_feedback_manager ON feedback (manager_id);
