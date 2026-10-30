package com.tokoku.outbox;

import java.util.Objects;
import java.util.UUID;

/**
 * Satu event yang akan dikirim relay.
 *
 * @param eventId ID unik event, dipakai consumer untuk dedup
 * @param aggregateType mis. "Stock"
 * @param aggregateId key Kafka: urutan per aggregate terjaga
 * @param eventType mis. "StockRejected", dikirim sebagai header {@code eventType}
 * @param topic topic tujuan
 * @param payload JSON event
 */
public record OutboxMessage(UUID eventId, String aggregateType, String aggregateId, String eventType, String topic,
		String payload) {

	public OutboxMessage {
		Objects.requireNonNull(eventId, "eventId");
		Objects.requireNonNull(aggregateId, "aggregateId");
		Objects.requireNonNull(eventType, "eventType");
		Objects.requireNonNull(topic, "topic");
		Objects.requireNonNull(payload, "payload");
	}

}
