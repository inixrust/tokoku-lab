package com.tokoku.order.infrastructure.adapters.out.persistence;

import java.util.Optional;

import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.port.out.OrderStore;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * L6.1: adapter persistence. Buat OrderEntity, OrderItemEntity, repository Spring Data,
 * dan mapper entity <-> domain di package ini, lalu implementasikan method di bawah.
 */
@Component
class JpaOrderStore implements OrderStore {

	@Override
	public Order save(Order order) {
		throw new UnsupportedOperationException("L6.1: belum diimplementasikan");
	}

	@Override
	public Optional<Order> findById(long id) {
		throw new UnsupportedOperationException("L6.1: belum diimplementasikan");
	}

	@Override
	public Page<Order> findAll(Pageable pageable) {
		throw new UnsupportedOperationException("L6.1: belum diimplementasikan");
	}

}
