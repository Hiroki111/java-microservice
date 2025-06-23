-- Migration: create_orders
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
