# outbox-support

Komponen Transactional Outbox untuk service TokoKu (Modul 7). Dipakai **inventory-service**.
order-service sengaja **tidak** memakainya: di L7.1, peserta menulis outbox dan relay order-service sendiri.

| Bean | Fungsi |
|---|---|
| `OutboxWriter` | Menulis `OutboxMessage` ke tabel `outbox` di transaction pemanggil (wajib ada transaction). |
| `OutboxRelay` | `@Scheduled` (default 500 ms): ambil batch `FOR UPDATE SKIP LOCKED`, kirim ke Kafka (key = `aggregate_id`, header `eventType`/`eventId`/`traceparent`), tunggu ack 5 s, tandai `published_at`. Matikan dengan `tokoku.outbox.relay-enabled=false`. |
| `ProcessedEvents` | `markIfNew(eventId)` untuk consumer idempotent (`INSERT ... ON CONFLICT DO NOTHING`). |

Tabel yang dibutuhkan (Flyway di service pemakai):

```sql
CREATE TABLE outbox (
    id              bigserial     PRIMARY KEY,
    event_id        uuid          NOT NULL UNIQUE,
    aggregate_type  varchar(40)   NOT NULL,
    aggregate_id    varchar(64)   NOT NULL,
    event_type      varchar(60)   NOT NULL,
    topic           varchar(100)  NOT NULL,
    payload         text          NOT NULL,
    created_at      timestamptz   NOT NULL DEFAULT now(),
    published_at    timestamptz,
    traceparent     varchar(64)     -- W3C trace context saat event ditulis (Modul 20)
);
CREATE INDEX outbox_unpublished_idx ON outbox (id) WHERE published_at IS NULL;

CREATE TABLE processed_event (
    event_id      uuid         PRIMARY KEY,
    processed_at  timestamptz  NOT NULL DEFAULT now()
);
```
