package com.tokoku.order.infrastructure.adapters.in.rest.dto;

import java.time.Instant;
import java.util.List;

import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.model.OrderStatus;

public record OrderResponse(long id, String customerId, OrderStatus status, Instant createdAt, List<Line> lines) {

	public record Line(long productId, int quantity) {
	}

	public static OrderResponse from(Order order) {
		List<Line> lines = order.lines().stream().map(line -> new Line(line.productId(), line.quantity())).toList();
		return new OrderResponse(order.id(), order.customerId(), order.status(), order.createdAt(), lines);
	}

}
