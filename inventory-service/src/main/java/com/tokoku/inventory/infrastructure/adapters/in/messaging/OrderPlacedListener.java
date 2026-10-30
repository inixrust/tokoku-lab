package com.tokoku.inventory.infrastructure.adapters.in.messaging;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.tokoku.inventory.application.StockService;
import com.tokoku.inventory.domain.model.ReservationLine;
import com.tokoku.inventory.domain.model.ReservationResult;
import com.tokoku.inventory.infrastructure.adapters.out.messaging.StockEventPublisher;
import com.tokoku.outbox.ProcessedEvents;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Consumer idempotent (slide 61): dedup, efek bisnis, dan event hasil dalam satu transaction. */
@Component
class OrderPlacedListener {

	private static final Logger log = LoggerFactory.getLogger(OrderPlacedListener.class);

	private final ProcessedEvents processedEvents;

	private final StockService stocks;

	private final StockEventPublisher publisher;

	private final JsonMapper json;

	OrderPlacedListener(ProcessedEvents processedEvents, StockService stocks, StockEventPublisher publisher,
			JsonMapper json) {
		this.processedEvents = processedEvents;
		this.stocks = stocks;
		this.publisher = publisher;
		this.json = json;
	}

	@KafkaListener(topics = OrderPlaced.TOPIC)
	@Transactional
	void on(ConsumerRecord<String, String> record) {
		Header type = record.headers().lastHeader("eventType");
		if (type == null || !OrderPlaced.TYPE.equals(new String(type.value(), StandardCharsets.UTF_8))) {
			return;
		}
		OrderPlaced event = json.readValue(record.value(), OrderPlaced.class);
		if (!processedEvents.markIfNew(event.eventId())) {
			log.info("Duplicate OrderPlaced {} for order {} ignored", event.eventId(), event.orderId());
			return;
		}
		List<ReservationLine> lines = event.lines().stream()
			.map(line -> new ReservationLine(line.productId(), line.quantity()))
			.toList();
		switch (stocks.reserve(lines)) {
			case ReservationResult.Reserved reserved -> {
				publisher.stockReserved(event.orderId());
				log.info("Stock reserved for order {}", event.orderId());
			}
			case ReservationResult.Rejected(long productId, int requested, int available) -> {
				publisher.stockRejected(event.orderId(), productId,
						"requested " + requested + ", available " + available);
				log.info("Stock rejected for order {}: product {} requested {} available {}", event.orderId(),
						productId, requested, available);
			}
		}
	}

}
