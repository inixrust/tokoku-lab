package com.tokoku.order.infrastructure.adapters.out.messaging;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Relay terjadwal, terpisah dari request user (slide 60). Bila crash setelah send dan sebelum commit,
 * event dikirim ulang: at-least-once, sehingga consumer wajib idempotent.
 */
@Component
class OutboxRelay {

	private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

	private final OutboxEventRepository outbox;

	private final KafkaTemplate<String, String> kafka;

	OutboxRelay(OutboxEventRepository outbox, KafkaTemplate<String, String> kafka) {
		this.outbox = outbox;
		this.kafka = kafka;
	}

	@Scheduled(fixedDelay = 500)
	@Transactional
	public void relay() throws Exception {
		List<OutboxEventEntity> batch = outbox.lockNextBatch();
		for (OutboxEventEntity event : batch) {
			ProducerRecord<String, String> record = new ProducerRecord<>(event.getTopic(), event.getAggregateId(),
					event.getPayload());
			record.headers().add("eventType", event.getEventType().getBytes(StandardCharsets.UTF_8));
			record.headers().add("eventId", event.getEventId().toString().getBytes(StandardCharsets.UTF_8));
			kafka.send(record).get(5, TimeUnit.SECONDS); // ack broker + timeout
			event.markPublished();
		}
		if (!batch.isEmpty()) {
			log.info("Outbox relay published {} event(s)", batch.size());
		}
	}

}
