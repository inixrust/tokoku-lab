package com.tokoku.order.domain.model;

/** Product milik product-service: disimpan sebagai ID saja, bukan relationship (slide 43). */
public record OrderLine(long productId, int quantity) {

	public OrderLine {
		if (quantity < 1) {
			throw new IllegalArgumentException("quantity must be at least 1");
		}
	}

}
