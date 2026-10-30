package com.tokoku.order.infrastructure.adapters.in.messaging;

import java.util.UUID;

/**
 * Event dari inventory-service di topic {@code stock-events} (key = orderId).
 * Header {@code eventType}: {@code StockReserved} atau {@code StockRejected}.
 * {@code productId} dan {@code reason} hanya terisi pada StockRejected.
 */
public record StockEvent(UUID eventId, long orderId, Long productId, String reason) {

	public static final String TOPIC = "stock-events";

	public static final String RESERVED = "StockReserved";

	public static final String REJECTED = "StockRejected";

}
