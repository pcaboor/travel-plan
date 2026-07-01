-- Subscriptions reuse the bookings table. Denormalize the travel start date so
-- the unsubscribe cutoff (>= 3 days before departure) can be enforced locally
-- in admin-service without a cross-service call to travel-service.
ALTER TABLE bookings ADD COLUMN travel_start_date DATE;
