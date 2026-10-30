package com.tokoku.order.domain.port.out;

import java.util.Optional;

import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.model.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderStore {

	Order save(Order order);

	/** Tanpa filter pemilik: hanya untuk proses internal (saga), bukan untuk request user. */
	Optional<Order> findById(long id);

	Optional<Order> findByIdAndCustomer(long id, String customerId);

	Page<Order> findAllByCustomer(String customerId, Pageable pageable);

	/** Mengubah status order yang sudah ada (optimistic locking lewat @Version). */
	void updateStatus(long orderId, OrderStatus status);

}
