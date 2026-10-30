package com.tokoku.order.infrastructure.adapters.out.http;

import com.tokoku.order.domain.model.Availability;
import com.tokoku.order.domain.port.out.StockCheck;

import org.springframework.stereotype.Component;

@Component
class InventoryStockAdapter implements StockCheck {

	private final InventoryClient inventory;

	InventoryStockAdapter(InventoryClient inventory) {
		this.inventory = inventory;
	}

	@Override
	public Availability check(long productId, int quantity) {
		StockResponse stock = inventory.stock(productId);
		return (stock.available() >= quantity) ? Availability.AVAILABLE : Availability.INSUFFICIENT;
	}

}
