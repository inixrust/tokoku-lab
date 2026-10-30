package com.tokoku.order.infrastructure.adapters.out.messaging;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Baris tabel outbox (slide 59): id, aggregate_id (key Kafka), type, payload, created_at, published_at. */
@Entity
@Table(name = "outbox")
class OutboxEventEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private UUID eventId;

	@Column(nullable = false, length = 40)
	private String aggregateType;

	@Column(nullable = false, length = 64)
	private String aggregateId;

	@Column(nullable = false, length = 60)
	private String eventType;

	@Column(nullable = false, length = 100)
	private String topic;

	@Column(nullable = false)
	private String payload;

	@Column(nullable = false)
	private Instant createdAt;

	private Instant publishedAt;

	protected OutboxEventEntity() {
	}

	static OutboxEventEntity of(UUID eventId, String aggregateType, String aggregateId, String eventType,
			String topic, String payload) {
		OutboxEventEntity entity = new OutboxEventEntity();
		entity.eventId = eventId;
		entity.aggregateType = aggregateType;
		entity.aggregateId = aggregateId;
		entity.eventType = eventType;
		entity.topic = topic;
		entity.payload = payload;
		entity.createdAt = Instant.now();
		return entity;
	}

	void markPublished() {
		publishedAt = Instant.now();
	}

	UUID getEventId() {
		return eventId;
	}

	String getAggregateId() {
		return aggregateId;
	}

	String getEventType() {
		return eventType;
	}

	String getTopic() {
		return topic;
	}

	String getPayload() {
		return payload;
	}

}
