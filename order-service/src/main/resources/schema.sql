CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    customer_name VARCHAR(255) NOT NULL
);
