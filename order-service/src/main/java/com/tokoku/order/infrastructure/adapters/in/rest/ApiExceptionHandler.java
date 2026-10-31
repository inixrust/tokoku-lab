package com.tokoku.order.infrastructure.adapters.in.rest;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.tokoku.order.domain.exception.InsufficientStockException;
import com.tokoku.order.domain.exception.OrderNotFoundException;
import com.tokoku.order.payment.domain.IdempotencyException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Semua error dalam format RFC 9457, pola yang sama dengan product-service (L5.1).
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	private static final String PROBLEM_BASE = "https://api.tokoku.example/problems/";

	@ExceptionHandler(OrderNotFoundException.class)
	ProblemDetail handleNotFound(OrderNotFoundException ex) {
		ProblemDetail pd = problem(HttpStatus.NOT_FOUND, "order-not-found", "Order not found",
				"Order " + ex.orderId() + " was not found", "ORDER_NOT_FOUND");
		pd.setProperty("orderId", ex.orderId());
		return pd;
	}

	@ExceptionHandler(InsufficientStockException.class)
	ProblemDetail handleInsufficientStock(InsufficientStockException ex) {
		ProblemDetail pd = problem(HttpStatus.CONFLICT, "insufficient-stock", "Insufficient stock",
				"Not enough stock for product " + ex.productId(), "INSUFFICIENT_STOCK");
		pd.setProperty("productId", ex.productId());
		return pd;
	}

	/** Idempotency-Key dipakai ulang dengan payload berbeda: 422; diproses paralel: 409 (slide 81). */
	@ExceptionHandler(IdempotencyException.class)
	ProblemDetail handleIdempotency(IdempotencyException ex) {
		ProblemDetail pd = switch (ex) {
			case IdempotencyException.KeyReused reused -> problem(HttpStatus.UNPROCESSABLE_CONTENT,
					"idempotency-key-reused", "Idempotency-Key reused", ex.getMessage(), "IDEMPOTENCY_KEY_REUSED");
			case IdempotencyException.Conflict conflict -> problem(HttpStatus.CONFLICT, "idempotency-conflict",
					"Concurrent request", ex.getMessage(), "IDEMPOTENCY_CONFLICT");
		};
		pd.setProperty("idempotencyKey", ex.key());
		return pd;
	}

	/** Downstream tidak terjangkau (connection refused, timeout): 503, bukan 500. */
	@ExceptionHandler(ResourceAccessException.class)
	ProblemDetail handleDownstreamUnavailable(ResourceAccessException ex) {
		log.warn("Downstream call failed: {}", ex.getMessage());
		return problem(HttpStatus.SERVICE_UNAVAILABLE, "downstream-unavailable", "Dependency unavailable",
				"A required service is temporarily unavailable. Please retry later.", "DOWNSTREAM_UNAVAILABLE");
	}

	/** Invariant domain dilanggar oleh input client (mis. order tanpa item). */
	@ExceptionHandler(IllegalArgumentException.class)
	ProblemDetail handleInvalidOrder(IllegalArgumentException ex) {
		return problem(HttpStatus.BAD_REQUEST, "invalid-order", "Invalid order", ex.getMessage(), "INVALID_ORDER");
	}

	/** Parameter sort berisi property yang tidak ada: kesalahan client, bukan 500. */
	@ExceptionHandler(PropertyReferenceException.class)
	ProblemDetail handleBadSort(PropertyReferenceException ex) {
		return problem(HttpStatus.BAD_REQUEST, "invalid-sort", "Invalid sort property",
				"Cannot sort by '" + ex.getPropertyName() + "'", "INVALID_SORT");
	}

	/** Jalur 1: {@code @Valid @RequestBody}. */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<Map<String, Object>> errors = ex.getBindingResult().getFieldErrors().stream()
			.map(error -> fieldError(error.getField(), error))
			.toList();
		return validationProblem(ex, errors, headers, status, request);
	}

	/** Jalur 2: constraint langsung pada parameter handler (mis. {@code @Size} pada {@code q}). */
	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<Map<String, Object>> errors = ex.getParameterValidationResults().stream()
			.flatMap(result -> result.getResolvableErrors().stream()
				.map(error -> fieldError(result.getMethodParameter().getParameterName(), error)))
			.toList();
		return validationProblem(ex, errors, headers, status, request);
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
		// Detail teknis dan stack trace hanya di log.
		log.error("Unhandled error {} {}", request.getMethod(), request.getRequestURI(), ex);
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				"Unexpected error. Contact support with the traceId.");
		pd.setProperty("errorCode", "INTERNAL_ERROR");
		pd.setProperty("traceId", MDC.get("traceId"));
		return pd;
	}

	private ResponseEntity<Object> validationProblem(Exception ex, List<Map<String, Object>> errors,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "validation-error", "Invalid request",
				"One or more fields are invalid", "VALIDATION_ERROR");
		pd.setProperty("errors", errors);
		return handleExceptionInternal(ex, pd, headers, status, request);
	}

	private static Map<String, Object> fieldError(String field, MessageSourceResolvable error) {
		String name = (error instanceof FieldError fieldError) ? fieldError.getField() : field;
		return Map.of("field", name, "message", String.valueOf(error.getDefaultMessage()));
	}

	private static ProblemDetail problem(HttpStatus status, String type, String title, String detail,
			String errorCode) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
		pd.setType(URI.create(PROBLEM_BASE + type));
		pd.setTitle(title);
		pd.setProperty("errorCode", errorCode);
		return pd;
	}

}
