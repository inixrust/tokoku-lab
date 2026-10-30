package com.tokoku.inventory.application;

import com.tokoku.inventory.domain.exception.StockNotFoundException;
import com.tokoku.inventory.domain.model.Stock;
import com.tokoku.inventory.domain.port.out.StockStore;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StockService {

	private final StockStore stocks;

	public StockService(StockStore stocks) {
		this.stocks = stocks;
	}

	public Stock get(long productId) {
		return stocks.findByProductId(productId).orElseThrow(() -> new StockNotFoundException(productId));
	}

}
