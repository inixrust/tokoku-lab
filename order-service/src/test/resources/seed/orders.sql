-- Data uji eksplisit L19.1 (selain seed Flyway): dua order milik cust-7 dan satu milik cust-8.
INSERT INTO orders (id, customer_id, status, created_at, version) VALUES
    (9001, 'cust-7', 'PENDING',   now(), 0),
    (9002, 'cust-7', 'CONFIRMED', now(), 0),
    (9003, 'cust-8', 'PENDING',   now(), 0);
INSERT INTO order_items (id, order_id, product_id, quantity) VALUES
    (9001, 9001, 1, 1),
    (9002, 9002, 2, 2),
    (9003, 9003, 3, 1);
