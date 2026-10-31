package com.tokoku.product.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;

import com.tokoku.product.domain.model.Product;

/** Representasi API, bukan entity JPA (slide 33). */
public record ProductV1(long id, String name, BigDecimal price) {

	public static ProductV1 from(Product product) {
		return new ProductV1(product.id(), product.name(), product.price());
	}

}
