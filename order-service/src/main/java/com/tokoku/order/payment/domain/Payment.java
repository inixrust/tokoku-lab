package com.tokoku.order.payment.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Payment(Long id, long orderId, BigDecimal amount, String method, Status status, Instant createdAt) {

	public enum Status {

		CAPTURED

	}

	public static Payment capture(long orderId, BigDecimal amount, String method) {
		return new Payment(null, orderId, amount, method, Status.CAPTURED, Instant.now());
	}

}
