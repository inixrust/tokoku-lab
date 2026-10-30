package com.tokoku.inventory.infrastructure.adapters.in.rest;

import com.tokoku.inventory.domain.model.Stock;

record StockResponse(long productId, int available) {

	static StockResponse from(Stock stock) {
		return new StockResponse(stock.productId(), stock.available());
	}

}
