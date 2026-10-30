package com.tokoku.product.domain.model;

import java.math.BigDecimal;

/** Model domain: tanpa anotasi Spring atau JPA (slide 25). */
public record Product(Long id, String sku, String name, BigDecimal price) {

	public static Product newProduct(String sku, String name, BigDecimal price) {
		return new Product(null, sku, name, price);
	}

}
