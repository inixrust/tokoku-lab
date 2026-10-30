package com.tokoku.order.infrastructure.adapters.in.messaging;

import java.nio.charset.StandardCharsets;

import com.tokoku.order.application.service.OrderSagaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class StockEventsListener {

	private static final Logger log = LoggerFactory.getLogger(StockEventsListener.class);

	private final OrderSagaService saga;

	private final JsonMapper json;

	StockEventsListener(OrderSagaService saga, JsonMapper json) {
		this.saga = saga;
		this.json = json;
	}

	@KafkaListener(topics = StockEvent.TOPIC)
	void on(ConsumerRecord<String, String> record) {
		Header type = record.headers().lastHeader("eventType");
		String eventType = (type != null) ? new String(type.value(), StandardCharsets.UTF_8) : "";
		StockEvent event = json.readValue(record.value(), StockEvent.class);
		switch (eventType) {
			case StockEvent.RESERVED -> saga.stockReserved(event.eventId(), event.orderId());
			case StockEvent.REJECTED -> saga.stockRejected(event.eventId(), event.orderId(), event.reason());
			default -> log.warn("Unknown event type '{}' on {}", eventType, StockEvent.TOPIC);
		}
	}

}
