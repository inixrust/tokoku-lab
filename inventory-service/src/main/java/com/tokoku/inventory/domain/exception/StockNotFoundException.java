package com.tokoku.inventory.domain.exception;

public class StockNotFoundException extends RuntimeException {

	private final long productId;

	public StockNotFoundException(long productId) {
		super("No stock record for product " + productId);
		this.productId = productId;
	}

	public long productId() {
		return productId;
	}

}
