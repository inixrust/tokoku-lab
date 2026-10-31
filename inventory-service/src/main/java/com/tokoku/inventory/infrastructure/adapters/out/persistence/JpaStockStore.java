package com.tokoku.inventory.infrastructure.adapters.out.persistence;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tokoku.inventory.domain.exception.StockNotFoundException;
import com.tokoku.inventory.domain.model.Stock;
import com.tokoku.inventory.domain.port.out.StockStore;

import org.springframework.stereotype.Component;

@Component
class JpaStockStore implements StockStore {

	private final StockJpaRepository repository;

	JpaStockStore(StockJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Stock> findByProductId(long productId) {
		return repository.findById(productId).map(StockEntity::toDomain);
	}

	@Override
	public Map<Long, Integer> lockAvailable(Collection<Long> productIds) {
		return repository.lockAll(productIds).stream()
			.collect(Collectors.toMap(StockEntity::getProductId, StockEntity::getAvailable));
	}

	@Override
	public void decrement(long productId, int quantity) {
		// Entity sudah terkunci dan managed di transaction ini: perubahan di-flush saat commit.
		repository.findById(productId).orElseThrow(() -> new StockNotFoundException(productId)).decrement(quantity);
	}

}
