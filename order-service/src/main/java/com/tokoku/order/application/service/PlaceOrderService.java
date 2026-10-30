package com.tokoku.order.application.service;

import com.tokoku.order.application.port.in.PlaceOrderUseCase;
import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.port.out.OrderStore;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PlaceOrderService implements PlaceOrderUseCase {

	private final OrderStore orders;

	PlaceOrderService(OrderStore orders) {
		this.orders = orders;
	}

	@Override
	@Transactional
	public Order place(PlaceOrderCommand command) {
		Order order = Order.place(command.customerId(), command.lines());
		return orders.save(order);
	}

}
