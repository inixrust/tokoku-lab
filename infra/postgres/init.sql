-- Satu instance PostgreSQL, satu schema + satu user per service (database per service, slide 63).
-- Dijalankan otomatis saat volume pertama kali dibuat, dan ulang lewat scripts/reset-db.ps1.
-- Idempoten: aman dijalankan berkali-kali.

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'product_user') THEN
        CREATE ROLE product_user LOGIN PASSWORD 'product_pw';
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'order_user') THEN
        CREATE ROLE order_user LOGIN PASSWORD 'order_pw';
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'inventory_user') THEN
        CREATE ROLE inventory_user LOGIN PASSWORD 'inventory_pw';
    END IF;
END
$$;

CREATE SCHEMA IF NOT EXISTS product   AUTHORIZATION product_user;
CREATE SCHEMA IF NOT EXISTS orders    AUTHORIZATION order_user;
CREATE SCHEMA IF NOT EXISTS inventory AUTHORIZATION inventory_user;

-- Setiap user hanya melihat schema miliknya; tidak ada tabel bersama.
ALTER ROLE product_user   SET search_path = product;
ALTER ROLE order_user     SET search_path = orders;
ALTER ROLE inventory_user SET search_path = inventory;

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA product, orders, inventory FROM PUBLIC;
