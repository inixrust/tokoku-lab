package com.tokoku.inventory.domain.port.out;

import java.util.Optional;

import com.tokoku.inventory.domain.model.Stock;

public interface StockStore {

	Optional<Stock> findByProductId(long productId);

}
