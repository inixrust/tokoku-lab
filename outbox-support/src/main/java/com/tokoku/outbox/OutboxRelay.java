package com.tokoku.outbox;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

/**
 * Relay outbox yang aman untuk banyak instance (slide 60): {@code FOR UPDATE SKIP LOCKED} membuat setiap
 * instance mengambil batch berbeda. Gagal sebelum ditandai terkirim berarti dikirim ulang (at-least-once).
 */
public class OutboxRelay {

	private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

	private final JdbcClient jdbc;

	private final KafkaTemplate<String, String> kafka;

	private final int batchSize;

	public OutboxRelay(JdbcClient jdbc, KafkaTemplate<String, String> kafka, int batchSize) {
		this.jdbc = jdbc;
		this.kafka = kafka;
		this.batchSize = batchSize;
	}

	@Scheduled(fixedDelayString = "${tokoku.outbox.relay-interval:500ms}")
	@Transactional
	public void relay() throws Exception {
		List<Row> batch = jdbc.sql("""
				select id, event_id, aggregate_id, event_type, topic, payload, traceparent from outbox
				where published_at is null
				order by id
				limit :limit
				for update skip locked
				""")
			.param("limit", batchSize)
			.query(Row.class)
			.list();
		for (Row row : batch) {
			ProducerRecord<String, String> record = new ProducerRecord<>(row.topic(), row.aggregateId(), row.payload());
			record.headers().add("eventType", row.eventType().getBytes(StandardCharsets.UTF_8));
			record.headers().add("eventId", row.eventId().toString().getBytes(StandardCharsets.UTF_8));
			if (row.traceparent() != null) {
				record.headers().add("traceparent", row.traceparent().getBytes(StandardCharsets.UTF_8));
			}
			// Tunggu ack broker dengan timeout.
			kafka.send(record).get(5, TimeUnit.SECONDS);
			jdbc.sql("update outbox set published_at = now() where id = ?").param(row.id()).update();
		}
		if (!batch.isEmpty()) {
			log.info("Outbox relay published {} event(s)", batch.size());
		}
	}

	record Row(long id, UUID eventId, String aggregateId, String eventType, String topic, String payload,
			String traceparent) {
	}

}
