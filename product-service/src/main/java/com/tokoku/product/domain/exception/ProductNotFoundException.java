package com.tokoku.product.domain.exception;

public class ProductNotFoundException extends RuntimeException {

	private final long productId;

	public ProductNotFoundException(long productId) {
		super("Product " + productId + " not found");
		this.productId = productId;
	}

	public long productId() {
		return productId;
	}

}
