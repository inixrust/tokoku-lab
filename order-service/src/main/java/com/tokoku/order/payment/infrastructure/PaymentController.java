package com.tokoku.order.payment.infrastructure;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;

import com.tokoku.order.payment.application.PaymentService;
import com.tokoku.order.payment.application.PaymentService.PaymentCommand;
import com.tokoku.order.payment.domain.Payment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PaymentController {

	private final PaymentService payments;

	PaymentController(PaymentService payments) {
		this.payments = payments;
	}

	/** Key dari client; satu key = satu operasi bisnis (slide 81). */
	@PostMapping("/api/payments")
	ResponseEntity<PaymentView> pay(@RequestHeader("Idempotency-Key") @NotBlank @Size(max = 100) String key,
			@Valid @RequestBody PaymentRequest request) {
		PaymentService.Result result = payments.payOnce(key,
				new PaymentCommand(request.orderId(), request.amount(), request.method()));
		Payment payment = result.payment();
		return ResponseEntity.status(HttpStatus.CREATED)
			.location(URI.create("/api/payments/" + payment.id()))
			.header("Idempotent-Replayed", String.valueOf(result.replayed()))
			.body(PaymentView.from(payment));
	}

	record PaymentRequest(@Positive long orderId, @Positive @DecimalMax("100000000") BigDecimal amount,
			@NotBlank @Pattern(regexp = "CARD|VA|EWALLET") String method) {
	}

	record PaymentView(long id, long orderId, BigDecimal amount, String method, String status, Instant createdAt) {

		static PaymentView from(Payment payment) {
			return new PaymentView(payment.id(), payment.orderId(), payment.amount(), payment.method(),
					payment.status().name(), payment.createdAt());
		}

	}

}
