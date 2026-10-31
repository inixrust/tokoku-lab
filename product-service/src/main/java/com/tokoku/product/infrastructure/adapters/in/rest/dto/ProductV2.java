package com.tokoku.product.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;

import com.tokoku.product.domain.model.Product;

/** v2: menambah sku dan harga sebagai objek uang (breaking change bagi client v1). */
public record ProductV2(long id, String sku, String name, Money price) {

	public record Money(BigDecimal amount, String currency) {
	}

	public static ProductV2 from(Product product) {
		return new ProductV2(product.id(), product.sku(), product.name(), new Money(product.price(), "IDR"));
	}

}
