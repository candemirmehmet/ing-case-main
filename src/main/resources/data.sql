-- Insert test data into customer table
INSERT INTO customer (username, password, user_role)
VALUES ('admin', 'adminpass', 'ADMIN');
INSERT INTO customer (username, password, user_role)
VALUES ('john', 'password123', 'CUSTOMER');
INSERT INTO customer (username, password, user_role)
VALUES ('jane', 'password456', 'CUSTOMER');
INSERT INTO customer (username, password, user_role)
VALUES ('user', 'dummy', 'CUSTOMER');

-- Insert customer's shares
INSERT INTO assets (size, usable_size, customer_id, asset_name)
VALUES (1000.00, 800.00, 2, 'AAPL');
INSERT INTO assets (size, usable_size, customer_id, asset_name)
VALUES (500.00, 300.00, 3, 'GOOGL');

-- Insert customer's TRY Funds
INSERT INTO assets (size, usable_size, customer_id, asset_name)
VALUES (0.00, 10000.00, 1, 'TRY');
INSERT INTO assets (size, usable_size, customer_id, asset_name)
VALUES (0.00, 3000.00, 2, 'TRY');
INSERT INTO assets (size, usable_size, customer_id, asset_name)
VALUES (0.00, 8000.00, 3, 'TRY');
INSERT INTO assets (size, usable_size, customer_id, asset_name)
VALUES (0.00, 900.00, 4, 'TRY');

-- Insert test data into orders table
INSERT INTO orders (price, size, create_date, customer_id, asset_name, order_side, status)
VALUES (150.00, 10, '2023-10-01 10:00:00', 2, 'AAPL', 'BUY', 'PENDING');
INSERT INTO orders (price, size, create_date, customer_id, asset_name, order_side, status)
VALUES (200.00, 5, '2023-10-01 11:00:00', 2, 'AAPL', 'SELL', 'MATCHED');
INSERT INTO orders (price, size, create_date, customer_id, asset_name, order_side, status)
VALUES (250.00, 2, '2023-10-01 12:00:00', 3, 'GOOGL', 'BUY', 'CANCELED');
INSERT INTO orders (price, size, create_date, customer_id, asset_name, order_side, status)
VALUES (300.00, 1, '2023-10-01 13:00:00', 3, 'GOOGL', 'SELL', 'PENDING');
