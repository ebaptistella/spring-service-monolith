--liquibase formatted sql

--changeset monolith:002-identity-create-accounts-table
CREATE TABLE IF NOT EXISTS accounts (
    id                     UUID PRIMARY KEY,
    email                  VARCHAR(255) NOT NULL,
    status                 VARCHAR(32)  NOT NULL,
    created_at             TIMESTAMPTZ  NOT NULL,
    idempotency_key        UUID         NOT NULL,
    root_idempotency_key   UUID         NOT NULL,
    request_fingerprint    VARCHAR(64)  NOT NULL,
    CONSTRAINT uk_accounts_email UNIQUE (email),
    CONSTRAINT uk_accounts_idempotency_key UNIQUE (idempotency_key)
);

--changeset monolith:002-identity-create-account-identity-links-table
CREATE TABLE IF NOT EXISTS account_identity_links (
    id                UUID PRIMARY KEY,
    account_id        UUID         NOT NULL REFERENCES accounts (id),
    provider          VARCHAR(32)  NOT NULL,
    external_subject  VARCHAR(512) NOT NULL,
    linked_at         TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_account_identity_links_provider_subject UNIQUE (provider, external_subject)
);

CREATE INDEX IF NOT EXISTS idx_account_identity_links_account_id ON account_identity_links (account_id);

--changeset monolith:002-identity-create-account-roles-table
CREATE TABLE IF NOT EXISTS account_roles (
    account_id UUID        NOT NULL REFERENCES accounts (id),
    role       VARCHAR(32) NOT NULL,
    PRIMARY KEY (account_id, role)
);

--changeset monolith:002-identity-create-local-credentials-table
CREATE TABLE IF NOT EXISTS local_credentials (
    account_id    UUID PRIMARY KEY REFERENCES accounts (id),
    password_hash VARCHAR(255) NOT NULL
);
