package com.tokoku.order.infrastructure.adapters.out.http;

import com.tokoku.order.domain.model.Availability;
import com.tokoku.order.domain.port.out.StockCheck;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

/**
 * Urutan pertahanan (slide 77): timeout (spring.http.clients.read-timeout) -> retry terbatas + jitter
 * -> circuit breaker + fallback. Retry membungkus panggilan yang dijaga breaker.
 */
@Component
class InventoryStockAdapter implements StockCheck {

	private final InventoryClient inventory;

	private final CircuitBreaker breaker;

	InventoryStockAdapter(InventoryClient inventory, CircuitBreakerRegistry breakers) {
		this.inventory = inventory;
		this.breaker = breakers.circuitBreaker("inventory");
	}

	/**
	 * Hanya error transien (timeout, connection refused) yang di-retry: maks 1 + 2 attempt, exponential
	 * backoff + jitter (slide 80). GET stok idempotent, jadi aman di-retry. CallNotPermittedException tidak
	 * termasuk: saat breaker OPEN langsung fallback tanpa retry.
	 */
	@Override
	@Retryable(includes = ResourceAccessException.class, maxRetries = 2, delay = 200, multiplier = 2, jitter = 100,
			maxDelay = 1000)
	public Availability check(long productId, int quantity) {
		try {
			StockResponse stock = breaker.executeSupplier(() -> inventory.stock(productId));
			return (stock.available() >= quantity) ? Availability.AVAILABLE : Availability.INSUFFICIENT;
		}
		catch (CallNotPermittedException open) {
			// Fallback "cukup baik": order tetap dibuat PENDING, saga (L7.1) yang memutuskan stok.
			return Availability.UNKNOWN;
		}
	}

}
