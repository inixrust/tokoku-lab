package com.tokoku.order.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Aggregate Order: aturan bisnis di sini, tanpa Spring atau JPA. */
public record Order(Long id, String customerId, OrderStatus status, Instant createdAt, List<OrderLine> lines) {

	public Order {
		Objects.requireNonNull(customerId, "customerId");
		Objects.requireNonNull(status, "status");
		lines = List.copyOf(lines);
		if (lines.isEmpty()) {
			throw new IllegalArgumentException("order must have at least one line");
		}
	}

	public static Order place(String customerId, List<OrderLine> lines) {
		return new Order(null, customerId, OrderStatus.PENDING, Instant.now(), lines);
	}

	/** Saga: hanya order PENDING yang boleh dikonfirmasi atau dibatalkan. */
	public boolean canMoveTo(OrderStatus next) {
		return status == OrderStatus.PENDING && next != OrderStatus.PENDING;
	}

}
