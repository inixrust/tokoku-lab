package com.tokoku.outbox;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Menulis event ke tabel outbox di transaction milik pemanggil: data dan event atomik (slide 59). */
public class OutboxWriter {

	private final JdbcClient jdbc;

	public OutboxWriter(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void write(OutboxMessage message) {
		jdbc.sql("""
				insert into outbox (event_id, aggregate_type, aggregate_id, event_type, topic, payload)
				values (:eventId, :aggregateType, :aggregateId, :eventType, :topic, :payload)
				""")
			.paramSource(message)
			.update();
	}

}
