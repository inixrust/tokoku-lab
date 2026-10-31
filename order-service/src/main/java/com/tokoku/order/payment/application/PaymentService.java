package com.tokoku.order.payment.application;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

import com.tokoku.order.payment.domain.IdempotencyException;
import com.tokoku.order.payment.domain.IdempotencyStore;
import com.tokoku.order.payment.domain.Payment;
import com.tokoku.order.payment.domain.PaymentStore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Operasi finansial yang bisa di-retry client wajib idempotent (slide 81). */
@Service
public class PaymentService {

	private final PaymentStore payments;

	private final IdempotencyStore idempotency;

	/** Simulasi panggilan payment gateway eksternal (D10.3: 500ms agar request paralel benar-benar tumpang tindih). */
	private final Duration gatewayDelay;

	PaymentService(PaymentStore payments, IdempotencyStore idempotency,
			@Value("${tokoku.payments.gateway-delay:0ms}") Duration gatewayDelay) {
		this.payments = payments;
		this.idempotency = idempotency;
		this.gatewayDelay = gatewayDelay;
	}

	/**
	 * Key sudah ada dengan payload sama → kembalikan hasil tersimpan (replay). Payload beda → 422.
	 * Key baru → klaim key, eksekusi, simpan hasil, semuanya di satu transaction.
	 */
	@Transactional
	public Result payOnce(String key, PaymentCommand command) {
		String hash = hash(command);
		var previous = idempotency.find(key);
		if (previous.isPresent()) {
			return replay(key, hash, previous.get());
		}
		if (!idempotency.claim(key, hash)) {
			throw new IdempotencyException.Conflict(key);
		}
		callGateway();
		Payment payment = payments.save(Payment.capture(command.orderId(), command.amount(), command.method()));
		idempotency.complete(key, payment.id());
		return new Result(payment, false);
	}

	private Result replay(String key, String hash, IdempotencyStore.StoredRequest previous) {
		if (!previous.requestHash().equals(hash)) {
			throw new IdempotencyException.KeyReused(key);
		}
		if (previous.paymentId() == null) {
			throw new IdempotencyException.Conflict(key);
		}
		Payment payment = payments.findById(previous.paymentId()).orElseThrow();
		return new Result(payment, true);
	}

	private void callGateway() {
		if (gatewayDelay.isPositive()) {
			try {
				Thread.sleep(gatewayDelay);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private static String hash(PaymentCommand command) {
		String canonical = command.orderId() + "|" + command.amount().stripTrailingZeros().toPlainString() + "|"
				+ command.method();
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public record PaymentCommand(long orderId, BigDecimal amount, String method) {
	}

	/** @param replayed {@code true} bila hasil diambil dari request sebelumnya dengan key yang sama */
	public record Result(Payment payment, boolean replayed) {
	}

}
