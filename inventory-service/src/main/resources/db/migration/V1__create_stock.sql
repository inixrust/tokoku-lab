CREATE TABLE stock (
    product_id  bigint  PRIMARY KEY,
    available   int     NOT NULL CHECK (available >= 0),
    version     bigint  NOT NULL DEFAULT 0
);

-- Product 1..20 sesuai seed product-service.
-- Product 19 hanya 2 unit dan product 20 habis: untuk menguji stok tidak cukup.
INSERT INTO stock (product_id, available)
SELECT g, CASE g WHEN 19 THEN 2 WHEN 20 THEN 0 ELSE 100 END
FROM generate_series(1, 20) AS g;
