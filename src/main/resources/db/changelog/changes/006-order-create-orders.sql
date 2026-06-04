--liquibase formatted sql

--changeset monolith:006-order-create-orders-table
CREATE TABLE IF NOT EXISTS orders (
    id                   UUID PRIMARY KEY,
    customer_id          UUID           NOT NULL REFERENCES customers (id),
    status               VARCHAR(32)    NOT NULL,
    total_amount         NUMERIC(19, 2) NOT NULL,
    currency             VARCHAR(3)     NOT NULL DEFAULT 'BRL',
    created_at           TIMESTAMPTZ    NOT NULL,
    updated_at           TIMESTAMPTZ    NOT NULL,
    idempotency_key      UUID           NOT NULL,
    root_idempotency_key UUID           NOT NULL,
    request_fingerprint  VARCHAR(64)    NOT NULL,
    CONSTRAINT uk_orders_idempotency_key UNIQUE (idempotency_key)
);

--changeset monolith:006-order-create-order-lines-table
CREATE TABLE IF NOT EXISTS order_lines (
    id          UUID PRIMARY KEY,
    order_id    UUID           NOT NULL REFERENCES orders (id),
    sku_id      UUID           NOT NULL REFERENCES skus (id),
    quantity    INTEGER        NOT NULL,
    unit_price  NUMERIC(19, 2) NOT NULL,
    line_total  NUMERIC(19, 2) NOT NULL
);

--changeset monolith:006-order-customer-index
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders (customer_id);

--changeset monolith:006-order-status-index
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders (status);
