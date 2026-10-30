-- Mulai L10.1 ada load test (wrk2 ~50 rps): stok product 1..18 dibuat besar agar tidak habis di tengah uji
-- (stok habis = 409 = hasil uji tidak valid). Product 19 (stok 2) dan 20 (habis) tetap untuk skenario saga.
UPDATE stock SET available = 1000000 WHERE product_id <= 18;
