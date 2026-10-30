-- Data mode perf (L12.1, L15.1, capstone): 10.000 order milik alice (60%) dan bob (40%), masing-masing 3 item.
-- Dijalankan lewat scripts/seed-perf.ps1 SETELAH order-service pernah start (Flyway sudah membuat tabel).
-- Idempoten: ID 100001..110000 dihapus dulu lalu diisi ulang.

BEGIN;

DELETE FROM orders.order_items WHERE order_id BETWEEN 100001 AND 110000;
DELETE FROM orders.orders WHERE id BETWEEN 100001 AND 110000;

INSERT INTO orders.orders (id, customer_id, status, created_at, version)
SELECT 100000 + g,
       CASE WHEN g % 5 IN (0, 1) THEN '7f2c1a4e-0b1d-4c55-9a6e-b0b000000002'
            ELSE '7f2c1a4e-0b1d-4c55-9a6e-a11ce0000001' END,
       CASE WHEN g % 20 = 3 THEN 'CANCELLED' WHEN g % 7 = 0 THEN 'PENDING' ELSE 'CONFIRMED' END,
       now() - make_interval(mins => g * 50),
       0
FROM generate_series(1, 10000) AS g;

INSERT INTO orders.order_items (id, order_id, product_id, quantity)
SELECT 300000 + (o - 1) * 3 + i,
       100000 + o,
       ((o * 7 + i * 3) % 18) + 1,
       ((o + i) % 3) + 1
FROM generate_series(1, 10000) AS o, generate_series(1, 3) AS i;

COMMIT;

ANALYZE orders.orders;
ANALYZE orders.order_items;

SELECT customer_id, count(*) AS orders FROM orders.orders GROUP BY customer_id ORDER BY customer_id;
