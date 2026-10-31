package com.tokoku.inventory.infrastructure.adapters.out.messaging;

import java.util.UUID;

import com.tokoku.outbox.OutboxMessage;
import com.tokoku.outbox.OutboxWriter;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.stereotype.Component;

/**
 * Menulis hasil reservasi ke outbox (dikirim OutboxRelay dari outbox-support ke topic {@code stock-events},
 * key = orderId). Harus dipanggil di dalam transaction listener.
 */
@Component
public class StockEventPublisher {

	public static final String TOPIC = "stock-events";

	private final OutboxWriter outbox;

	private final JsonMapper json;

	StockEventPublisher(OutboxWriter outbox, JsonMapper json) {
		this.outbox = outbox;
		this.json = json;
	}

	public void stockReserved(long orderId) {
		publish("StockReserved", new StockEvent(UUID.randomUUID(), orderId, null, null));
	}

	public void stockRejected(long orderId, long productId, String reason) {
		publish("StockRejected", new StockEvent(UUID.randomUUID(), orderId, productId, reason));
	}

	private void publish(String type, StockEvent event) {
		outbox.write(new OutboxMessage(event.eventId(), "Order", String.valueOf(event.orderId()), type, TOPIC,
				json.writeValueAsString(event)));
	}

	/** Payload untuk StockReserved dan StockRejected; productId dan reason hanya terisi pada StockRejected. */
	record StockEvent(UUID eventId, long orderId, Long productId, String reason) {
	}

}
