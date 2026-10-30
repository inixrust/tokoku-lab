package com.tokoku.order.payment.infrastructure;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;

import com.tokoku.order.payment.domain.IdempotencyStore;
import com.tokoku.order.payment.domain.Payment;
import com.tokoku.order.payment.domain.PaymentStore;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Adapter persistence payment dengan JdbcClient: tabel sederhana, tidak butuh model JPA. */
@Component
class JdbcPaymentStores implements PaymentStore, IdempotencyStore {

	private final JdbcClient jdbc;

	JdbcPaymentStores(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public Payment save(Payment payment) {
		long id = jdbc.sql("select nextval('payments_seq')").query(Long.class).single();
		jdbc.sql("""
				insert into payments (id, order_id, amount, method, status, created_at)
				values (?, ?, ?, ?, ?, ?)""")
			.params(id, payment.orderId(), payment.amount(), payment.method(), payment.status().name(),
					Timestamp.from(payment.createdAt()))
			.update();
		return new Payment(id, payment.orderId(), payment.amount(), payment.method(), payment.status(),
				payment.createdAt());
	}

	@Override
	public Optional<Payment> findById(long id) {
		return jdbc.sql("select id, order_id, amount, method, status, created_at from payments where id = ?")
			.param(id)
			.query((rs, row) -> new Payment(rs.getLong("id"), rs.getLong("order_id"), rs.getObject("amount",
					BigDecimal.class), rs.getString("method"), Payment.Status.valueOf(rs.getString("status")),
					rs.getTimestamp("created_at").toInstant()))
			.optional();
	}

	@Override
	public Optional<StoredRequest> find(String key) {
		return jdbc.sql("select request_hash, payment_id from idempotency_keys where idempotency_key = ?")
			.param(key)
			.query((rs, row) -> new StoredRequest(rs.getString("request_hash"), rs.getObject("payment_id", Long.class)))
			.optional();
	}

	@Override
	public boolean claim(String key, String requestHash) {
		return jdbc.sql("""
				insert into idempotency_keys (idempotency_key, request_hash) values (?, ?)
				on conflict (idempotency_key) do nothing""")
			.params(key, requestHash)
			.update() == 1;
	}

	@Override
	public void complete(String key, long paymentId) {
		jdbc.sql("update idempotency_keys set payment_id = ? where idempotency_key = ?").params(paymentId, key).update();
	}

}
