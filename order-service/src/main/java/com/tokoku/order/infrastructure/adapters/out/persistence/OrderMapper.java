package com.tokoku.order.infrastructure.adapters.out.persistence;

import java.util.List;

import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.model.OrderLine;

/** Mapper eksplisit entity <-> domain. Dipanggil di dalam transaction (OSIV mati). */
final class OrderMapper {

	private OrderMapper() {
	}

	static OrderEntity toEntity(Order order) {
		OrderEntity entity = new OrderEntity(order.customerId(), order.status(), order.createdAt());
		order.lines().forEach(line -> entity.addItem(new OrderItemEntity(line.productId(), line.quantity())));
		return entity;
	}

	static Order toDomain(OrderEntity entity) {
		// Mengakses items memicu lazy load: satu query per order (N+1, dibahas di L15.1).
		List<OrderLine> lines = entity.getItems().stream()
			.map(item -> new OrderLine(item.getProductId(), item.getQuantity()))
			.toList();
		return new Order(entity.getId(), entity.getCustomerId(), entity.getStatus(), entity.getCreatedAt(), lines);
	}

}
