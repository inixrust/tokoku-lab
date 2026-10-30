package com.tokoku.order.payment.domain;

import java.util.Optional;

/** Penyimpanan Idempotency-Key (slide 81). */
public interface IdempotencyStore {

	Optional<StoredRequest> find(String key);

	/**
	 * Mengklaim key di transaction yang sedang berjalan. Bila transaction lain sedang memegang key yang sama,
	 * PostgreSQL menunggu sampai transaction itu selesai.
	 * @return {@code false} bila key sudah diklaim request lain (paralel)
	 */
	boolean claim(String key, String requestHash);

	void complete(String key, long paymentId);

	record StoredRequest(String requestHash, Long paymentId) {
	}

}
