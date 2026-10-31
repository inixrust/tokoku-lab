-- Transactional outbox + dedup consumer (Modul 7). Struktur sama dengan outbox-support.
CREATE TABLE outbox (
    id              bigserial     PRIMARY KEY,
    event_id        uuid          NOT NULL UNIQUE,
    aggregate_type  varchar(40)   NOT NULL,
    aggregate_id    varchar(64)   NOT NULL,
    event_type      varchar(60)   NOT NULL,
    topic           varchar(100)  NOT NULL,
    payload         text          NOT NULL,
    created_at      timestamptz   NOT NULL DEFAULT now(),
    published_at    timestamptz
);
CREATE INDEX outbox_unpublished_idx ON outbox (id) WHERE published_at IS NULL;

CREATE TABLE processed_event (
    event_id      uuid         PRIMARY KEY,
    processed_at  timestamptz  NOT NULL DEFAULT now()
);
