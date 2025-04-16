-- Insert dealers
 INSERT INTO dealers (name, address) VALUES ('Best Car Dealer', '123 Main St, Springfield');
 INSERT INTO dealers (name, address) VALUES ('City Wheels', '456 Elm St, Metropolis');

-- Insert products
INSERT INTO products (name, description, price, available, category, dealer_id)
VALUES ('Civic', 'Reliable compact car', 22000.00, true, 'SEDAN', 1);

INSERT INTO products (name, description, price, available, category, dealer_id)
VALUES ('RAV4', 'Spacious and powerful SUV', 32000.00, true, 'SUV', 2);

INSERT INTO products (name, description, price, available, category, dealer_id)
VALUES ('Accord', 'Comfortable family sedan', 28000.00, true, 'SEDAN', 1);
