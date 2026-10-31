package com.tokoku.outbox;

import java.util.function.Supplier;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Menulis event ke tabel outbox di transaction milik pemanggil: data dan event atomik (slide 59). */
public class OutboxWriter {

	private final JdbcClient jdbc;

	private final Supplier<String> traceparent;

	public OutboxWriter(JdbcClient jdbc) {
		this(jdbc, () -> null);
	}

	/** @param traceparent pemberi header W3C traceparent dari span aktif (boleh mengembalikan null) */
	public OutboxWriter(JdbcClient jdbc, Supplier<String> traceparent) {
		this.jdbc = jdbc;
		this.traceparent = traceparent;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void write(OutboxMessage message) {
		jdbc.sql("""
				insert into outbox (event_id, aggregate_type, aggregate_id, event_type, topic, payload, traceparent)
				values (:eventId, :aggregateType, :aggregateId, :eventType, :topic, :payload, :traceparent)
				""")
			.param("eventId", message.eventId())
			.param("aggregateType", message.aggregateType())
			.param("aggregateId", message.aggregateId())
			.param("eventType", message.eventType())
			.param("topic", message.topic())
			.param("payload", message.payload())
			.param("traceparent", traceparent.get())
			.update();
	}

}
