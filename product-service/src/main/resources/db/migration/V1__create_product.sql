CREATE SEQUENCE product_seq START WITH 1001 INCREMENT BY 50;

CREATE TABLE product (
    id          bigint         PRIMARY KEY,
    sku         varchar(40)    NOT NULL UNIQUE,
    name        varchar(120)   NOT NULL,
    price       numeric(12, 2) NOT NULL CHECK (price > 0),
    created_at  timestamptz    NOT NULL DEFAULT now()
);
