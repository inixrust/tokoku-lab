package com.tokoku.product.domain.exception;

public class DuplicateSkuException extends RuntimeException {

	private final String sku;

	public DuplicateSkuException(String sku) {
		super("SKU " + sku + " already exists");
		this.sku = sku;
	}

	public String sku() {
		return sku;
	}

}
