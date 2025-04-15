CREATE TABLE IF NOT EXISTS dealers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(100) NOT NULL,
    price DECIMAL NOT NULL,
    available BOOLEAN NOT NULL,
    category VARCHAR(50) NOT NULL,
    dealer_id BIGINT NOT NULL,
    CONSTRAINT fk_products_dealer
        FOREIGN KEY (dealer_id)
        REFERENCES dealers(id)
        ON DELETE CASCADE
);
