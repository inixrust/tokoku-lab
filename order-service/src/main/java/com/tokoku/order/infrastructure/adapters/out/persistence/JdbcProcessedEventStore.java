package com.tokoku.order.infrastructure.adapters.out.persistence;

import java.util.UUID;

import com.tokoku.order.domain.port.out.ProcessedEventStore;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
class JdbcProcessedEventStore implements ProcessedEventStore {

	private final JdbcClient jdbc;

	JdbcProcessedEventStore(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public boolean markIfNew(UUID eventId) {
		// Primary key event_id menjamin keunikan, juga bila dua consumer memproses event yang sama bersamaan.
		return jdbc.sql("insert into processed_event (event_id) values (?) on conflict do nothing")
			.param(eventId)
			.update() == 1;
	}

}
