-- allocationSize Hibernate = 50, jadi INCREMENT BY harus 50 (bila tidak, aplikasi gagal start).
-- Seed memakai ID 1..150, sequence mulai dari 1001.
CREATE SEQUENCE orders_seq      START WITH 1001 INCREMENT BY 50;
CREATE SEQUENCE order_items_seq START WITH 1001 INCREMENT BY 50;

CREATE TABLE orders (
    id           bigint       PRIMARY KEY,
    customer_id  varchar(64)  NOT NULL,
    status       varchar(20)  NOT NULL,
    created_at   timestamptz  NOT NULL,
    version      bigint       NOT NULL
);

CREATE TABLE order_items (
    id          bigint  PRIMARY KEY,
    order_id    bigint  NOT NULL REFERENCES orders (id),
    product_id  bigint  NOT NULL,
    quantity    int     NOT NULL CHECK (quantity > 0)
);

CREATE INDEX order_items_order_id_idx ON order_items (order_id);
