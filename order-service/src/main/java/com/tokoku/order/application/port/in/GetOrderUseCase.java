package com.tokoku.order.application.port.in;

import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.model.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetOrderUseCase {

	/** Ownership di query (slide 91): order milik orang lain diperlakukan sebagai tidak ada (404). */
	Order get(long id, String customerId);

	/** @param status filter opsional; {@code null} = semua status */
	Page<Order> list(String customerId, OrderStatus status, Pageable pageable);

}
