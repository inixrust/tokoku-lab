package com.tokoku.inventory.infrastructure.adapters.in.messaging;

import java.util.List;
import java.util.UUID;

/**
 * Event dari order-service di topic {@code orders} (header {@code eventType=OrderPlaced}).
 * Hanya field yang dibutuhkan inventory (Tolerant Reader): field lain diabaikan.
 */
public record OrderPlaced(UUID eventId, long orderId, List<Line> lines) {

	public static final String TOPIC = "orders";

	public static final String TYPE = "OrderPlaced";

	public record Line(long productId, int quantity) {
	}

}
