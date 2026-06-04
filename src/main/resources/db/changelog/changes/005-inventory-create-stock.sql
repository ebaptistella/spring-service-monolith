--liquibase formatted sql

--changeset monolith:005-inventory-create-stock-levels
CREATE TABLE IF NOT EXISTS stock_levels (
    sku_id     UUID PRIMARY KEY REFERENCES skus (id),
    on_hand    INTEGER NOT NULL DEFAULT 0,
    reserved   INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL
);

--changeset monolith:005-inventory-create-stock-reservations
CREATE TABLE IF NOT EXISTS stock_reservations (
    id              UUID PRIMARY KEY,
    order_id        UUID        NOT NULL,
    sku_id          UUID        NOT NULL REFERENCES skus (id),
    quantity        INTEGER     NOT NULL,
    status          VARCHAR(32) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    idempotency_key UUID        NOT NULL,
    CONSTRAINT uk_stock_reservations_order_sku UNIQUE (order_id, sku_id),
    CONSTRAINT uk_stock_reservations_idempotency_key UNIQUE (idempotency_key)
);

--changeset monolith:005-inventory-create-stock-movements
CREATE TABLE IF NOT EXISTS stock_movements (
    id              UUID PRIMARY KEY,
    sku_id          UUID        NOT NULL REFERENCES skus (id),
    order_id        UUID,
    quantity        INTEGER     NOT NULL,
    type            VARCHAR(32) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    idempotency_key UUID        NOT NULL,
    CONSTRAINT uk_stock_movements_idempotency_key UNIQUE (idempotency_key)
);

--changeset monolith:005-inventory-reservations-order-index
CREATE INDEX IF NOT EXISTS idx_stock_reservations_order_id ON stock_reservations (order_id);
