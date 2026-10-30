package com.tokoku.order.domain.exception;

public class InsufficientStockException extends RuntimeException {

	private final long productId;

	public InsufficientStockException(long productId) {
		super("Insufficient stock for product " + productId);
		this.productId = productId;
	}

	public long productId() {
		return productId;
	}

}
