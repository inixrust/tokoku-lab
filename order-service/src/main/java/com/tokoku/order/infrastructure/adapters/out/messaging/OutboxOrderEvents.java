package com.tokoku.order.infrastructure.adapters.out.messaging;

import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.port.out.OrderEvents;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Event tidak di-publish langsung: ditulis ke outbox, relay yang mengirim. */
@Component
class OutboxOrderEvents implements OrderEvents {

	private final OutboxEventRepository outbox;

	private final JsonMapper json;

	private final TraceContext trace;

	OutboxOrderEvents(OutboxEventRepository outbox, JsonMapper json, TraceContext trace) {
		this.outbox = outbox;
		this.json = json;
		this.trace = trace;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void orderPlaced(Order order) {
		OrderPlaced event = OrderPlaced.from(order);
		outbox.save(OutboxEventEntity.of(event.eventId(), "Order", String.valueOf(order.id()), OrderPlaced.TYPE,
				OrderPlaced.TOPIC, json.writeValueAsString(event), trace.currentTraceparent()));
	}

}
