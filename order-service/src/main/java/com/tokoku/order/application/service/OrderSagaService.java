package com.tokoku.order.application.service;

import java.util.UUID;

import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.model.OrderStatus;
import com.tokoku.order.domain.port.out.OrderStore;
import com.tokoku.order.domain.port.out.ProcessedEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Langkah saga di sisi order: hasil reservasi stok menentukan status akhir order (slide 62). */
@Service
public class OrderSagaService {

	private static final Logger log = LoggerFactory.getLogger(OrderSagaService.class);

	private final OrderStore orders;

	private final ProcessedEventStore processedEvents;

	OrderSagaService(OrderStore orders, ProcessedEventStore processedEvents) {
		this.orders = orders;
		this.processedEvents = processedEvents;
	}

	@Transactional
	public void stockReserved(UUID eventId, long orderId) {
		moveTo(eventId, orderId, OrderStatus.CONFIRMED);
	}

	/** Compensation: reservasi gagal, order dibatalkan. */
	@Transactional
	public void stockRejected(UUID eventId, long orderId, String reason) {
		log.info("Order {} rejected by inventory: {}", orderId, reason);
		moveTo(eventId, orderId, OrderStatus.CANCELLED);
	}

	private void moveTo(UUID eventId, long orderId, OrderStatus next) {
		// Dedup di transaction yang sama dengan perubahan status.
		if (!processedEvents.markIfNew(eventId)) {
			log.info("Duplicate event {} for order {} ignored", eventId, orderId);
			return;
		}
		Order order = orders.findById(orderId).orElse(null);
		if (order == null) {
			log.warn("Order {} not found for event {}", orderId, eventId);
			return;
		}
		if (!order.canMoveTo(next)) {
			log.info("Order {} already {}, ignoring {}", orderId, order.status(), next);
			return;
		}
		orders.updateStatus(orderId, next);
		log.info("Order {} -> {}", orderId, next);
	}

}
