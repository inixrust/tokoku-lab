-- Index mengikuti pola query nyata (slide 64): GET /api/orders?status=... milik satu customer, terbaru dulu.
-- Equality (customer_id, status) dulu, kolom ORDER BY terakhir. Leftmost column customer_id juga melayani
-- list tanpa filter status.
CREATE INDEX idx_orders_cust_status_created ON orders (customer_id, status, created_at DESC);
