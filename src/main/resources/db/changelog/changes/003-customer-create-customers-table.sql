--liquibase formatted sql

--changeset monolith:003-customer-create-customers-table
CREATE TABLE IF NOT EXISTS customers (
    id                   UUID PRIMARY KEY,
    email                VARCHAR(255) NOT NULL,
    full_name            VARCHAR(200) NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL,
    account_id           UUID NULL REFERENCES accounts (id),
    idempotency_key      UUID         NOT NULL,
    root_idempotency_key UUID         NOT NULL,
    request_fingerprint  VARCHAR(64)  NOT NULL,
    CONSTRAINT uk_customers_email UNIQUE (email),
    CONSTRAINT uk_customers_idempotency_key UNIQUE (idempotency_key)
);

--changeset monolith:003-customer-create-customers-email-index
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers (email);

--changeset monolith:003-customer-create-customers-account-index
CREATE INDEX IF NOT EXISTS idx_customers_account_id ON customers (account_id);
