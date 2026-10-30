package com.tokoku.product.infrastructure.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import com.tokoku.product.domain.model.Product;
import com.tokoku.product.domain.port.out.ProductStore;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
class JpaProductStore implements ProductStore {

	private final ProductJpaRepository repository;

	JpaProductStore(ProductJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Product> findById(long id) {
		return repository.findById(id).map(ProductEntity::toDomain);
	}

	@Override
	public Page<Product> findAll(Pageable pageable) {
		return repository.findAll(pageable).map(ProductEntity::toDomain);
	}

	@Override
	public List<Product> searchByName(String query) {
		return repository.findTop50ByNameContainingIgnoreCaseOrderByName(query).stream()
			.map(ProductEntity::toDomain)
			.toList();
	}

	@Override
	public boolean existsBySku(String sku) {
		return repository.existsBySku(sku);
	}

	@Override
	public Product save(Product product) {
		return repository.save(ProductEntity.from(product)).toDomain();
	}

}
