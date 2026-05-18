CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE roles (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(50)  NOT NULL UNIQUE,
    description  VARCHAR(255),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO roles (name, description) VALUES
    ('ADMIN',   'Full administrative access'),
    ('MANAGER', 'Operational management access'),
    ('VIEWER',  'Read-only access'),
    ('USER',    'Standard end-user access');

CREATE TABLE users (
    id              UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    status          VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT users_status_check CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_status ON users (status);

CREATE TABLE user_roles (
    user_id  UUID    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id  BIGINT  NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE payment_methods (
    id              UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider        VARCHAR(32)  NOT NULL,
    type            VARCHAR(32)  NOT NULL,
    provider_token  VARCHAR(255),
    last_four       VARCHAR(4),
    expires_at      DATE,
    status          VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT payment_methods_provider_check CHECK (provider IN ('STRIPE', 'PAYPAL')),
    CONSTRAINT payment_methods_type_check     CHECK (type IN ('CARD', 'BANK_ACCOUNT', 'WALLET')),
    CONSTRAINT payment_methods_status_check   CHECK (status IN ('ACTIVE', 'EXPIRED', 'REVOKED'))
);

CREATE INDEX idx_payment_methods_user ON payment_methods (user_id);

CREATE TABLE bookings (
    id                 UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id            UUID           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    travel_ref_id      UUID           NOT NULL,
    payment_method_id  UUID           REFERENCES payment_methods (id) ON DELETE SET NULL,
    amount             NUMERIC(12,2)  NOT NULL,
    currency           CHAR(3)        NOT NULL DEFAULT 'EUR',
    status             VARCHAR(32)    NOT NULL DEFAULT 'PENDING',
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT bookings_status_check  CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'REFUNDED')),
    CONSTRAINT bookings_amount_check  CHECK (amount >= 0)
);

CREATE INDEX idx_bookings_user      ON bookings (user_id);
CREATE INDEX idx_bookings_travel    ON bookings (travel_ref_id);
CREATE INDEX idx_bookings_payment   ON bookings (payment_method_id);
CREATE INDEX idx_bookings_status    ON bookings (status);
