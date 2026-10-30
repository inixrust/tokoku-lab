package com.tokoku.order.domain.port.out;

import com.tokoku.order.domain.model.Order;

/** Outbound port untuk event domain. Implementasi menulis ke outbox di transaction yang sedang berjalan. */
public interface OrderEvents {

	void orderPlaced(Order order);

}
