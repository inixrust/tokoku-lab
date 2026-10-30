package com.tokoku.inventory.infrastructure.adapters.out.persistence;

import java.util.Optional;

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

}
