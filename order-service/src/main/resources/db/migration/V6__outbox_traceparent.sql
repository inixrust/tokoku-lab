-- Modul 20: trace context ikut tersimpan bersama event, supaya relay meneruskannya ke Kafka
-- (header traceparent) dan consumer melanjutkan trace yang sama, walau dikirim asynchronous.
ALTER TABLE outbox ADD COLUMN traceparent varchar(64);
