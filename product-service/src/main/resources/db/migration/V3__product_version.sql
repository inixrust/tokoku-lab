-- L17.1: harga bisa diubah lewat PUT; @Version mencegah lost update (pola L6.1).
ALTER TABLE product ADD COLUMN version bigint NOT NULL DEFAULT 0;
