package com.tokoku.outbox;

import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Dedup untuk consumer idempotent (slide 61): primary key event_id menjamin keunikan. */
public class ProcessedEvents {

	private final JdbcClient jdbc;

	public ProcessedEvents(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	/** @return {@code true} bila event baru; {@code false} bila duplikat dan harus diabaikan */
	@Transactional(propagation = Propagation.MANDATORY)
	public boolean markIfNew(UUID eventId) {
		return jdbc.sql("insert into processed_event (event_id) values (?) on conflict do nothing")
			.param(eventId)
			.update() == 1;
	}

}
