CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE payment_transactions (
    id                  UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id             UUID         NOT NULL,
    booking_ref_id      UUID,
    provider            VARCHAR(32)  NOT NULL,
    provider_intent_id  VARCHAR(255),
    amount              NUMERIC(12,2) NOT NULL,
    currency            VARCHAR(3)   NOT NULL,
    status              VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    failure_reason      VARCHAR(500),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pt_provider_check CHECK (provider IN ('STRIPE', 'PAYPAL')),
    CONSTRAINT pt_status_check   CHECK (status IN (
        'PENDING', 'REQUIRES_ACTION', 'PROCESSING', 'SUCCEEDED',
        'FAILED', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT pt_amount_check   CHECK (amount >= 0)
);

CREATE UNIQUE INDEX uq_payment_transactions_intent
    ON payment_transactions (provider, provider_intent_id)
    WHERE provider_intent_id IS NOT NULL;
CREATE INDEX idx_payment_transactions_user    ON payment_transactions (user_id);
CREATE INDEX idx_payment_transactions_booking ON payment_transactions (booking_ref_id);
CREATE INDEX idx_payment_transactions_status  ON payment_transactions (status);
