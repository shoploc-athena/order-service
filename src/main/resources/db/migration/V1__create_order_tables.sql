-- V1__create_order_tables.sql
-- Creates initial order-service schema: cart, order checkout, orders, and history
-- All amounts in cents (BIGINT), timestamps in UTC (TIMESTAMPTZ)
-- Foreign keys to other services (customer_id, store_id, article_id, actor_id) are UUIDs only
-- No cascade delete on orders to prevent accidental data loss

-- =====================================================
-- Table: cart
-- Purpose: Shopping cart per customer (one per customer)
-- =====================================================
CREATE TABLE IF NOT EXISTS cart (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cart_customer_id ON cart(customer_id);

-- =====================================================
-- Table: cart_item
-- Purpose: Items in a shopping cart
-- Relationship: cart -> cart_item (1:N, cascade delete)
-- =====================================================
CREATE TABLE IF NOT EXISTS cart_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES cart(id) ON DELETE CASCADE,
    store_id UUID NOT NULL,
    article_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    added_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cart_item_cart_article UNIQUE (cart_id, article_id)
);

CREATE INDEX IF NOT EXISTS idx_cart_item_cart_id ON cart_item(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_item_store_id ON cart_item(store_id);

-- =====================================================
-- Table: order_checkout
-- Purpose: Checkout session before order confirmation
-- Idempotency: (customer_id, idempotency_key) must be unique OR both NULL
-- =====================================================
CREATE TABLE IF NOT EXISTS order_checkout (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    idempotency_key UUID,
    request_hash VARCHAR(255) NOT NULL,
    total_amount BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_checkout_customer_idempotency UNIQUE (customer_id, idempotency_key) WHERE idempotency_key IS NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_checkout_customer_id ON order_checkout(customer_id);
CREATE INDEX IF NOT EXISTS idx_checkout_idempotency ON order_checkout(idempotency_key) WHERE idempotency_key IS NOT NULL;

-- =====================================================
-- Table: customer_order
-- Purpose: Main order table with payment and status tracking
-- Relationships: checkout -> order (1:1), order -> order_lines (1:N)
-- Payment modes: ON_SITE, ONLINE, WALLET
-- Payment status: TO_PAY, IN_PROGRESS, PAID, FAILED, REFUND_DUE, REFUNDED
-- Order status: CONFIRMED, PREPARING, READY, COLLECTED, CANCELLED, EXPIRED
-- Optimistic locking: version column incremented on each update
-- =====================================================
CREATE TABLE IF NOT EXISTS customer_order (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    checkout_id UUID REFERENCES order_checkout(id),
    customer_id UUID NOT NULL,
    customer_display_name VARCHAR(255),
    store_id UUID NOT NULL,
    store_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    pickup_code VARCHAR(6),
    pickup_slot_start TIMESTAMPTZ NOT NULL,
    pickup_slot_end TIMESTAMPTZ NOT NULL,
    pickup_delay_min INTEGER,
    total_amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    payment_mode VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    payment_reference VARCHAR(255),
    expires_at TIMESTAMPTZ,
    collected_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    -- Constraints
    CONSTRAINT chk_order_status CHECK (status IN ('CONFIRMED', 'PREPARING', 'READY', 'COLLECTED', 'CANCELLED', 'EXPIRED')),
    CONSTRAINT chk_payment_mode CHECK (payment_mode IN ('ON_SITE', 'ONLINE', 'WALLET')),
    CONSTRAINT chk_payment_status CHECK (payment_status IN ('TO_PAY', 'IN_PROGRESS', 'PAID', 'FAILED', 'REFUND_DUE', 'REFUNDED')),
    CONSTRAINT chk_pickup_slot CHECK (pickup_slot_end > pickup_slot_start),
    CONSTRAINT chk_pickup_delay_min CHECK (pickup_delay_min IS NULL OR pickup_delay_min > 0)
);

CREATE INDEX IF NOT EXISTS idx_order_customer_created ON customer_order(customer_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_order_store_status_slot ON customer_order(store_id, status, pickup_slot_start);
CREATE INDEX IF NOT EXISTS idx_order_checkout_id ON customer_order(checkout_id);
CREATE INDEX IF NOT EXISTS idx_order_status_expires ON customer_order(status, expires_at) WHERE expires_at IS NOT NULL;

-- Partial unique constraint: same pickup_code can't be used in same store if order is active
CREATE UNIQUE INDEX IF NOT EXISTS uq_order_pickup_code_active 
ON customer_order(store_id, pickup_code) 
WHERE status IN ('CONFIRMED', 'PREPARING', 'READY');

-- =====================================================
-- Table: order_line
-- Purpose: Individual items in an order
-- Relationship: order -> order_line (1:N, no cascade delete)
-- Line total = unit_price * quantity (enforced by CHECK)
-- =====================================================
CREATE TABLE IF NOT EXISTS order_line (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES customer_order(id),
    article_id UUID NOT NULL,
    label VARCHAR(255) NOT NULL,
    unit_price BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    line_total BIGINT NOT NULL,
    -- Verify line_total calculation
    CONSTRAINT chk_line_total CHECK (line_total = unit_price * quantity)
);

CREATE INDEX IF NOT EXISTS idx_order_line_order_id ON order_line(order_id);

-- =====================================================
-- Table: order_status_history
-- Purpose: Track all status changes of an order
-- Relationship: order -> status_history (1:N, no cascade delete)
-- Status values: CONFIRMED, PREPARING, READY, COLLECTED, CANCELLED, EXPIRED
-- Actor: either a customer/user UUID or SYSTEM (from automated processes)
-- =====================================================
CREATE TABLE IF NOT EXISTS order_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES customer_order(id),
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    actor_id UUID,
    actor_role VARCHAR(50) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Constraints
    CONSTRAINT chk_from_status CHECK (from_status IS NULL OR from_status IN ('CONFIRMED', 'PREPARING', 'READY', 'COLLECTED', 'CANCELLED', 'EXPIRED')),
    CONSTRAINT chk_to_status CHECK (to_status IN ('CONFIRMED', 'PREPARING', 'READY', 'COLLECTED', 'CANCELLED', 'EXPIRED'))
);

CREATE INDEX IF NOT EXISTS idx_status_history_order_id ON order_status_history(order_id, changed_at DESC);
CREATE INDEX IF NOT EXISTS idx_status_history_actor ON order_status_history(actor_id, changed_at DESC) WHERE actor_id IS NOT NULL;

-- =====================================================
-- Trigger: Auto-update updated_at timestamp on cart
-- =====================================================
CREATE OR REPLACE FUNCTION update_cart_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_cart_updated_at
BEFORE UPDATE ON cart
FOR EACH ROW
EXECUTE FUNCTION update_cart_timestamp();

-- =====================================================
-- Trigger: Auto-update updated_at timestamp on customer_order
-- =====================================================
CREATE OR REPLACE FUNCTION update_order_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_order_updated_at
BEFORE UPDATE ON customer_order
FOR EACH ROW
EXECUTE FUNCTION update_order_timestamp();
