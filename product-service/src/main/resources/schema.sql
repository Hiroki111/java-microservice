CREATE TABLE IF NOT EXISTS dealers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(100) NOT NULL,
    price DECIMAL NOT NULL,
    available BOOLEAN NOT NULL,
    category VARCHAR(50) NOT NULL,
    dealer_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_products_dealer
        FOREIGN KEY (dealer_id)
        REFERENCES dealers(id)
        ON DELETE CASCADE
);
