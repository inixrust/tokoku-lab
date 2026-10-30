package com.tokoku.inventory.infrastructure.adapters.out.persistence;

import com.tokoku.inventory.domain.model.Stock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "stock")
class StockEntity {

	@Id
	private Long productId;

	@Column(nullable = false)
	private int available;

	@Version
	private long version;

	protected StockEntity() {
	}

	Stock toDomain() {
		return new Stock(productId, available);
	}

}
