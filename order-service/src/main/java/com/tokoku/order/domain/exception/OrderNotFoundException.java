package com.tokoku.order.domain.exception;

public class OrderNotFoundException extends RuntimeException {

	private final long orderId;

	public OrderNotFoundException(long orderId) {
		super("Order " + orderId + " not found");
		this.orderId = orderId;
	}

	public long orderId() {
		return orderId;
	}

}
