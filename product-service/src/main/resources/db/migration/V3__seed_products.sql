-- Migration: seed_products

INSERT INTO products (name, description, price, available, category, make, mileage, dealer_id)
VALUES
-- BMW
('BMW 3 Series', 'Sporty and efficient compact sedan.', 33000, true, 'SEDAN', 'BMW', 42000, 1),
('BMW X5', 'Luxury midsize SUV with spacious interior.', 45000, true, 'SUV', 'BMW', 38000, 2),
('BMW 1 Series', 'Compact hatchback for city driving.', 28000, true, 'HATCHBACK', 'BMW', 25000, 3),
('BMW 2 Series Gran Tourer', 'Versatile van with upscale design.', 35000, true, 'VAN', 'BMW', 30000, 4),

-- FORD
('Ford Fusion', 'Reliable and comfortable daily sedan.', 22000, true, 'SEDAN', 'FORD', 56000, 5),
('Ford Explorer', 'Spacious SUV perfect for family trips.', 37000, true, 'SUV', 'FORD', 47000, 6),
('Ford Focus', 'Compact hatchback with sporty feel.', 18000, true, 'HATCHBACK', 'FORD', 51000, 7),
('Ford Transit Connect', 'Small van ideal for city logistics.', 26000, true, 'VAN', 'FORD', 19000, 8),

-- HONDA
('Honda Accord', 'Spacious and fuel-efficient sedan.', 24000, true, 'SEDAN', 'HONDA', 48000, 9),
('Honda CR-V', 'Top-selling reliable SUV.', 32000, true, 'SUV', 'HONDA', 36000, 10),
('Honda Fit', 'Ultra-compact hatchback with clever storage.', 17000, true, 'HATCHBACK', 'HONDA', 23000, 1),
('Honda Odyssey', 'Family van with excellent safety.', 30000, true, 'VAN', 'HONDA', 40000, 2),

-- HYUNDAI
('Hyundai Elantra', 'Value-packed sedan.', 21000, true, 'SEDAN', 'HYUNDAI', 39000, 3),
('Hyundai Santa Fe', 'Comfortable and spacious SUV.', 29000, true, 'SUV', 'HYUNDAI', 28000, 4),
('Hyundai i30', 'Hatchback with modern tech.', 20000, true, 'HATCHBACK', 'HYUNDAI', 22000, 5),
('Hyundai Staria', 'Futuristic van for large families.', 33000, true, 'VAN', 'HYUNDAI', 31000, 6),

-- KIA
('Kia K5', 'Sporty midsize sedan.', 23000, true, 'SEDAN', 'KIA', 34000, 7),
('Kia Sorento', 'All-purpose SUV with third-row seating.', 31000, true, 'SUV', 'KIA', 45000, 8),
('Kia Ceed', 'Compact hatchback built for Europe.', 19500, true, 'HATCHBACK', 'KIA', 37000, 9),
('Kia Carnival', 'Family-friendly van.', 29000, true, 'VAN', 'KIA', 27000, 10),

-- TESLA
('Tesla Model 3', 'Fully electric performance sedan.', 41000, true, 'SEDAN', 'TESLA', 15000, 1),
('Tesla Model X', 'Luxury electric SUV with Falcon doors.', 55000, true, 'SUV', 'TESLA', 30000, 2),
('Tesla Model Y', 'Compact electric crossover.', 47000, true, 'HATCHBACK', 'TESLA', 12000, 3),
('Tesla Cybervan', 'Futuristic electric van prototype.', 50000, true, 'VAN', 'TESLA', 8000, 4),

-- TOYOTA
('Toyota Camry', 'Reliable midsize sedan.', 22000, true, 'SEDAN', 'TOYOTA', 44000, 5),
('Toyota RAV4', 'Popular and efficient SUV.', 26000, true, 'SUV', 'TOYOTA', 39000, 6),
('Toyota Yaris', 'Compact hatchback for city driving.', 18000, true, 'HATCHBACK', 'TOYOTA', 21000, 7),
('Toyota Sienna', 'Dependable minivan.', 31000, true, 'VAN', 'TOYOTA', 35000, 8),

-- VOLKSWAGEN
('VW Passat', 'Comfortable family sedan.', 21000, true, 'SEDAN', 'VOLKSWAGEN', 50000, 9),
('VW Tiguan', 'Refined SUV with solid handling.', 25000, true, 'SUV', 'VOLKSWAGEN', 43000, 10),
('VW Golf', 'Classic European hatchback.', 22000, true, 'HATCHBACK', 'VOLKSWAGEN', 27000, 1),
('VW Caddy', 'Compact and efficient city van.', 24000, true, 'VAN', 'VOLKSWAGEN', 29000, 2);
