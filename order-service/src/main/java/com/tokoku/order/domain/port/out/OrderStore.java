package com.tokoku.order.domain.port.out;

import java.util.Optional;

import com.tokoku.order.domain.model.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderStore {

	Order save(Order order);

	Optional<Order> findById(long id);

	Page<Order> findAll(Pageable pageable);

}
