package com.tokoku.notification.infrastructure.adapters.in.messaging;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Consumer group sendiri (notification-service): menerima setiap OrderPlaced, terpisah dari inventory. */
@Component
class OrderPlacedListener {

	private static final Logger log = LoggerFactory.getLogger(OrderPlacedListener.class);

	private final JsonMapper json;

	OrderPlacedListener(JsonMapper json) {
		this.json = json;
	}

	@KafkaListener(topics = OrderPlaced.TOPIC)
	void on(ConsumerRecord<String, String> record) {
		Header type = record.headers().lastHeader("eventType");
		if (type == null || !OrderPlaced.TYPE.equals(new String(type.value(), StandardCharsets.UTF_8))) {
			return;
		}
		OrderPlaced event = json.readValue(record.value(), OrderPlaced.class);
		// Saat redelivery, email bisa terkirim dua kali. Untuk lab cukup log; di produksi dedup eventId seperti inventory.
		log.info("Kirim email ke customer {}: order {} diterima ({} item)", event.customerId(), event.orderId(),
				event.lines().size());
	}

}
