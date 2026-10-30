-- Dipanggil scripts/reset-db.ps1: hapus semua schema service (termasuk riwayat Flyway),
-- lalu buat ulang. Data contoh dimuat ulang oleh Flyway saat service berikutnya start.
DROP SCHEMA IF EXISTS product   CASCADE;
DROP SCHEMA IF EXISTS orders    CASCADE;
DROP SCHEMA IF EXISTS inventory CASCADE;
\i /docker-entrypoint-initdb.d/00-init.sql
