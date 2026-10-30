package com.tokoku.product.infrastructure.adapters.out.persistence;

import java.math.BigDecimal;

import com.tokoku.product.domain.model.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
class ProductEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
	@SequenceGenerator(name = "product_seq", sequenceName = "product_seq", allocationSize = 50)
	private Long id;

	@Column(nullable = false, unique = true, length = 40)
	private String sku;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	protected ProductEntity() {
	}

	static ProductEntity from(Product product) {
		ProductEntity entity = new ProductEntity();
		entity.id = product.id();
		entity.sku = product.sku();
		entity.name = product.name();
		entity.price = product.price();
		return entity;
	}

	Product toDomain() {
		return new Product(id, sku, name, price);
	}

}
