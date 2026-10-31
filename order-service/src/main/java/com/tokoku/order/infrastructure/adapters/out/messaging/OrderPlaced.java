package com.tokoku.order.infrastructure.adapters.out.messaging;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.tokoku.order.domain.model.Order;

/**
 * Event state transfer ke topic {@code orders} (key = orderId, header {@code eventType=OrderPlaced}).
 * Konsumen: inventory-service (reservasi stok) dan notification-service.
 */
public record OrderPlaced(UUID eventId, long orderId, String customerId, List<Line> lines, Instant occurredAt) {

	public static final String TYPE = "OrderPlaced";

	public static final String TOPIC = "orders";

	public record Line(long productId, int quantity) {
	}

	public static OrderPlaced from(Order order) {
		List<Line> lines = order.lines().stream().map(line -> new Line(line.productId(), line.quantity())).toList();
		return new OrderPlaced(UUID.randomUUID(), order.id(), order.customerId(), lines, Instant.now());
	}

}
