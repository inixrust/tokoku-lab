package com.tokoku.order.application.service;

import com.tokoku.order.application.port.in.GetOrderUseCase;
import com.tokoku.order.domain.exception.OrderNotFoundException;
import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.model.OrderStatus;
import com.tokoku.order.domain.port.out.OrderStore;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class GetOrderService implements GetOrderUseCase {

	private final OrderStore orders;

	GetOrderService(OrderStore orders) {
		this.orders = orders;
	}

	@Override
	public Order get(long id, String customerId) {
		return orders.findByIdAndCustomer(id, customerId).orElseThrow(() -> new OrderNotFoundException(id));
	}

	@Override
	public Page<Order> list(String customerId, OrderStatus status, Pageable pageable) {
		return orders.findAllByCustomer(customerId, status, pageable);
	}

}
