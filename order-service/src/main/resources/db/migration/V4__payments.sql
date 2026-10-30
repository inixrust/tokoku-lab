-- Modul payment di order-service (keputusan rencana #7), dibangun pada L10.1-solution, dipakai L12.2.
CREATE SEQUENCE payments_seq START WITH 1001 INCREMENT BY 50;

CREATE TABLE payments (
    id          bigint          PRIMARY KEY,
    order_id    bigint          NOT NULL,
    amount      numeric(14, 2)  NOT NULL CHECK (amount > 0),
    method      varchar(20)     NOT NULL,
    status      varchar(20)     NOT NULL,
    created_at  timestamptz     NOT NULL
);

-- Satu Idempotency-Key = satu operasi bisnis (slide 81). PRIMARY KEY mencegah eksekusi ganda paralel.
CREATE TABLE idempotency_keys (
    idempotency_key  varchar(100)  PRIMARY KEY,
    request_hash     char(64)      NOT NULL,
    payment_id       bigint        REFERENCES payments (id),
    created_at       timestamptz   NOT NULL DEFAULT now()
);
