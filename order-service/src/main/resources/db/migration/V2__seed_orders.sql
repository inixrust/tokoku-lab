-- Data contoh L6.1: 50 order x 3 item, supaya N+1 terlihat jelas.
-- customer_id = ID user Keycloak yang tetap (realm tokoku, mulai L11.1):
--   alice = 7f2c1a4e-0b1d-4c55-9a6e-a11ce0000001
--   bob   = 7f2c1a4e-0b1d-4c55-9a6e-b0b000000002
-- Butuh V1__init.sql (ditulis peserta) dengan tabel dan kolom sesuai panduan L6.1.

INSERT INTO orders (id, customer_id, status, created_at, version)
SELECT g,
       CASE WHEN g % 5 IN (0, 1) THEN '7f2c1a4e-0b1d-4c55-9a6e-b0b000000002'
            ELSE '7f2c1a4e-0b1d-4c55-9a6e-a11ce0000001' END,
       CASE g % 10 WHEN 0 THEN 'CANCELLED' WHEN 1 THEN 'PENDING' ELSE 'CONFIRMED' END,
       now() - make_interval(hours => g * 7),
       0
FROM generate_series(1, 50) AS g;

INSERT INTO order_items (id, order_id, product_id, quantity)
SELECT (o - 1) * 3 + i,
       o,
       ((o * 7 + i * 3) % 20) + 1,
       ((o + i) % 3) + 1
FROM generate_series(1, 50) AS o, generate_series(1, 3) AS i;
