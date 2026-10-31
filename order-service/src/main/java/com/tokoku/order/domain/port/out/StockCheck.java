package com.tokoku.order.domain.port.out;

import com.tokoku.order.domain.model.Availability;

/** Outbound port ke inventory-service. */
public interface StockCheck {

	Availability check(long productId, int quantity);

}
