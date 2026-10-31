package com.tokoku.order.payment.domain;

/** Pelanggaran aturan Idempotency-Key. */
public abstract sealed class IdempotencyException extends RuntimeException {

	private final String key;

	private IdempotencyException(String key, String message) {
		super(message);
		this.key = key;
	}

	public String key() {
		return key;
	}

	/** Key yang sama dipakai untuk payload berbeda: 422. */
	public static final class KeyReused extends IdempotencyException {

		public KeyReused(String key) {
			super(key, "Idempotency-Key " + key + " was already used with a different request");
		}

	}

	/** Request lain dengan key yang sama sedang/sudah diproses bersamaan: 409. */
	public static final class Conflict extends IdempotencyException {

		public Conflict(String key) {
			super(key, "A request with Idempotency-Key " + key + " is being processed");
		}

	}

}
