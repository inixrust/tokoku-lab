package com.tokoku.notification.infrastructure.adapters.in.messaging;

import java.util.List;
import java.util.UUID;

/**
 * Event dari order-service di topic {@code orders} (header {@code eventType=OrderPlaced}).
 * L8.1-B: buat listener (consumer group sendiri: notification-service) yang mencetak log untuk setiap order.
 */
public record OrderPlaced(UUID eventId, long orderId, String customerId, List<Line> lines) {

	public static final String TOPIC = "orders";

	public static final String TYPE = "OrderPlaced";

	public record Line(long productId, int quantity) {
	}

}
