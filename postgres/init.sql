-- ==========================================
-- FlashFlow PostgreSQL Schema Initialization
-- ==========================================

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    price NUMERIC(19, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    initial_stock INTEGER NOT NULL,
    image_url VARCHAR(255),
    category VARCHAR(255),
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_product_stock_non_negative CHECK (stock_quantity >= 0)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(255) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    product_title VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    payment_reference VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_orders_idempotency_key ON orders (idempotency_key);
CREATE INDEX IF NOT EXISTS idx_orders_product_id ON orders (product_id);
CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders (status, created_at);

CREATE TABLE IF NOT EXISTS payment_records (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(255) NOT NULL UNIQUE,
    order_number VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(255),
    webhook_id VARCHAR(255),
    gateway_response VARCHAR(1000),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payment_order_number ON payment_records (order_number);

CREATE TABLE IF NOT EXISTS processed_webhooks (
    webhook_id VARCHAR(255) PRIMARY KEY,
    order_number VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    amount NUMERIC(19, 2),
    signature VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    payload_summary VARCHAR(2000),
    processed_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_webhook_order ON processed_webhooks (order_number);

-- Seed Initial High-Contention Flash Sale Products
INSERT INTO products (title, description, price, stock_quantity, initial_stock, image_url, category, version)
VALUES
('Sony PlayStation 5 Pro (Limited Edition)', 'Next-gen gaming console with 2TB high-speed SSD and AI-enhanced PSSR upscaling.', 699.99, 5, 5, 'https://images.unsplash.com/photo-1606813907291-d86efa9b94db?w=600&auto=format&fit=crop&q=80', 'Gaming', 0),
('Apple iPhone 16 Pro Max (Titanium Desert)', 'A18 Pro Bionic processor, 48MP Fusion camera, and revolutionary battery life.', 1199.99, 10, 10, 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=600&auto=format&fit=crop&q=80', 'Smartphones', 0),
('NVIDIA GeForce RTX 5090 OC 32GB', 'Ultra-enthusiast Blackwell architecture GPU for 8K gaming and local LLM AI inference.', 1999.99, 3, 3, 'https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?w=600&auto=format&fit=crop&q=80', 'Components', 0),
('Keychron Q1 Pro Custom Mechanical Keyboard', 'Full aluminum CNC body, hot-swappable switches, wireless Bluetooth 5.1.', 199.99, 15, 15, 'https://images.unsplash.com/photo-1595225476474-87563907a212?w=600&auto=format&fit=crop&q=80', 'Accessories', 0)
ON CONFLICT DO NOTHING;
