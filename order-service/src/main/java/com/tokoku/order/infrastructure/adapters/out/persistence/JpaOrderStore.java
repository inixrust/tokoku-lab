package com.tokoku.order.infrastructure.adapters.out.persistence;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.tokoku.order.domain.exception.OrderNotFoundException;
import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.model.OrderStatus;
import com.tokoku.order.domain.port.out.OrderStore;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
class JpaOrderStore implements OrderStore {

	private final OrderJpaRepository repository;

	JpaOrderStore(OrderJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Order save(Order order) {
		return OrderMapper.toDomain(repository.save(OrderMapper.toEntity(order)));
	}

	@Override
	public Optional<Order> findById(long id) {
		return repository.findById(id).map(OrderMapper::toDomain);
	}

	@Override
	public Optional<Order> findByIdAndCustomer(long id, String customerId) {
		return repository.findByIdAndCustomerId(id, customerId).map(OrderMapper::toDomain);
	}

	@Override
	public Page<Order> findAllByCustomer(String customerId, OrderStatus status, Pageable pageable) {
		// Dua tahap (slide 128): halaman ID + count, lalu satu query fetch items. Total 3 query, bukan 1 + N + 1.
		Page<OrderJpaRepository.IdOnly> ids = (status != null)
				? repository.findIdsByCustomerIdAndStatus(customerId, status, pageable)
				: repository.findIdsByCustomerId(customerId, pageable);
		List<Long> idList = ids.map(OrderJpaRepository.IdOnly::id).getContent();
		if (idList.isEmpty()) {
			return new PageImpl<>(List.of(), pageable, ids.getTotalElements());
		}
		List<Order> orders = repository.findByIdIn(idList).stream()
			.sorted(Comparator.comparingInt(entity -> idList.indexOf(entity.getId())))
			.map(OrderMapper::toDomain)
			.toList();
		return new PageImpl<>(orders, pageable, ids.getTotalElements());
	}

	@Override
	public void updateStatus(long orderId, OrderStatus status) {
		OrderEntity entity = repository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
		entity.setStatus(status);
	}

}
