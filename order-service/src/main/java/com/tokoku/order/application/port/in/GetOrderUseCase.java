package com.tokoku.order.application.port.in;

import com.tokoku.order.domain.model.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetOrderUseCase {

	Order get(long id);

	Page<Order> list(Pageable pageable);

}
