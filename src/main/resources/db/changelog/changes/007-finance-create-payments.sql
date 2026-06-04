--liquibase formatted sql

--changeset monolith:007-finance-create-payment-intents
CREATE TABLE IF NOT EXISTS payment_intents (
    id              UUID PRIMARY KEY,
    order_id        UUID           NOT NULL UNIQUE REFERENCES orders (id),
    amount          NUMERIC(19, 2) NOT NULL,
    currency        VARCHAR(3)     NOT NULL DEFAULT 'BRL',
    status          VARCHAR(32)    NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL,
    updated_at      TIMESTAMPTZ    NOT NULL,
    idempotency_key      UUID           NOT NULL,
    root_idempotency_key UUID           NOT NULL,
    CONSTRAINT uk_payment_intents_idempotency_key UNIQUE (idempotency_key)
);

--changeset monolith:007-finance-create-transactions
CREATE TABLE IF NOT EXISTS transactions (
    id                UUID PRIMARY KEY,
    payment_intent_id UUID        NOT NULL REFERENCES payment_intents (id),
    status            VARCHAR(32) NOT NULL,
    gateway_reference VARCHAR(128),
    created_at        TIMESTAMPTZ NOT NULL,
    idempotency_key   UUID        NOT NULL,
    CONSTRAINT uk_transactions_idempotency_key UNIQUE (idempotency_key)
);

--changeset monolith:007-finance-payment-intent-order-index
CREATE INDEX IF NOT EXISTS idx_payment_intents_order_id ON payment_intents (order_id);
