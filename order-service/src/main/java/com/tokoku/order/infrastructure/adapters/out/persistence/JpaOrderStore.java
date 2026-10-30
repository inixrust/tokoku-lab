package com.tokoku.order.infrastructure.adapters.out.persistence;

import java.util.Optional;

import com.tokoku.order.domain.exception.OrderNotFoundException;
import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.model.OrderStatus;
import com.tokoku.order.domain.port.out.OrderStore;

import org.springframework.data.domain.Page;
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
		Page<OrderEntity> page = (status != null) ? repository.findByCustomerIdAndStatus(customerId, status, pageable)
				: repository.findByCustomerId(customerId, pageable);
		return page.map(OrderMapper::toDomain);
	}

	@Override
	public void updateStatus(long orderId, OrderStatus status) {
		OrderEntity entity = repository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
		entity.setStatus(status);
	}

}
