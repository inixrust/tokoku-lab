package com.tokoku.order.infrastructure.adapters.out.http;

import com.tokoku.order.domain.model.Availability;
import com.tokoku.order.domain.port.out.StockCheck;

import org.springframework.stereotype.Component;

/**
 * L8.1-A: ganti stub ini dengan panggilan ke inventory-service lewat InventoryClient (@HttpExchange).
 * Endpoint inventory: GET /api/inventory/{productId} -> {"productId": 1, "available": 100}
 */
@Component
class InventoryStockAdapter implements StockCheck {

	@Override
	public Availability check(long productId, int quantity) {
		return Availability.AVAILABLE;
	}

}
