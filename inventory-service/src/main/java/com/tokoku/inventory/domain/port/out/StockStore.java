package com.tokoku.inventory.domain.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.tokoku.inventory.domain.model.Stock;

public interface StockStore {

	Optional<Stock> findByProductId(long productId);

	/**
	 * Mengunci baris stok (SELECT ... FOR UPDATE, urut product_id agar tidak deadlock).
	 * @return stok tersedia per product; product tanpa baris stok tidak ada di map
	 */
	Map<Long, Integer> lockAvailable(Collection<Long> productIds);

	/** Mengurangi stok yang sudah dikunci di transaction yang sama. */
	void decrement(long productId, int quantity);

}
