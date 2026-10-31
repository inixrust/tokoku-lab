package com.tokoku.order.application.port.in;

import java.util.List;

import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.model.OrderLine;

public interface PlaceOrderUseCase {

	Order place(PlaceOrderCommand command);

	record PlaceOrderCommand(String customerId, List<OrderLine> lines) {
	}

}
