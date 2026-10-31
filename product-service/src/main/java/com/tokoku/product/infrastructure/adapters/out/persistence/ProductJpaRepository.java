package com.tokoku.product.infrastructure.adapters.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

	List<ProductEntity> findTop50ByNameContainingIgnoreCaseOrderByName(String name);

	boolean existsBySku(String sku);

}
