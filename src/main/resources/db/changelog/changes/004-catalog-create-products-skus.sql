--liquibase formatted sql

--changeset monolith:004-catalog-create-products-table
CREATE TABLE IF NOT EXISTS products (
    id                   UUID PRIMARY KEY,
    name                 VARCHAR(200) NOT NULL,
    description          TEXT,
    active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMPTZ  NOT NULL,
    idempotency_key      UUID         NOT NULL,
    root_idempotency_key UUID         NOT NULL,
    request_fingerprint  VARCHAR(64)  NOT NULL,
    CONSTRAINT uk_products_idempotency_key UNIQUE (idempotency_key)
);

--changeset monolith:004-catalog-create-skus-table
CREATE TABLE IF NOT EXISTS skus (
    id                   UUID PRIMARY KEY,
    product_id           UUID           NOT NULL REFERENCES products (id),
    code                 VARCHAR(64)    NOT NULL,
    list_price           NUMERIC(19, 2) NOT NULL,
    active               BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMPTZ    NOT NULL,
    idempotency_key      UUID           NOT NULL,
    root_idempotency_key UUID           NOT NULL,
    request_fingerprint  VARCHAR(64)    NOT NULL,
    CONSTRAINT uk_skus_code UNIQUE (code),
    CONSTRAINT uk_skus_idempotency_key UNIQUE (idempotency_key)
);

--changeset monolith:004-catalog-skus-product-index
CREATE INDEX IF NOT EXISTS idx_skus_product_id ON skus (product_id);
