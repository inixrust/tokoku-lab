package com.tokoku.order.infrastructure.adapters.out.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.tokoku.order.domain.port.out.StockCheck;
import com.tokoku.order.infrastructure.config.InventoryProps;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.autoconfigure.HttpClientAutoConfiguration;
import org.springframework.boot.http.client.autoconfigure.imperative.ImperativeHttpClientAutoConfiguration;
import org.springframework.boot.http.client.autoconfigure.service.HttpServiceClientPropertiesAutoConfiguration;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.service.HttpServiceClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * L19.1 langkah 4: inventory lambat (WireMock) → timeout, lalu fallback.
 * Kerangka sudah menyiapkan WireMock dan context minimal (hanya HTTP client + adapter, tanpa DB/Kafka).
 *
 * <p>TODO:
 * <ul>
 * <li>Stub GET /api/inventory/.. dengan withFixedDelay(6000): InventoryClient melempar ResourceAccessException
 * setelah sekitar 5 s (read-timeout).</li>
 * <li>Breaker OPEN (breakers.circuitBreaker("inventory").transitionToOpenState()): adapter mengembalikan
 * Availability.UNKNOWN tanpa memanggil WireMock sama sekali.</li>
 * <li>Hapus fallback di InventoryStockAdapter, lalu pastikan test kedua GAGAL.</li>
 * </ul>
 */
@SpringBootTest(classes = InventoryTimeoutTest.HttpOnly.class,
		properties = { "spring.http.clients.connect-timeout=1s",
				// Eksplisit: nilai produksi hasil L10.1 bisa berbeda, sehingga delay 6 s tidak selalu memicu timeout.
				"spring.http.clients.read-timeout=5s" })
class InventoryTimeoutTest {

	static final WireMockServer inventory = new WireMockServer(options().dynamicPort());

	static {
		inventory.start();
	}

	@DynamicPropertySource
	static void inventoryUrl(DynamicPropertyRegistry registry) {
		registry.add("tokoku.inventory.base-url", inventory::baseUrl);
	}

	@AfterAll
	static void stopInventory() {
		inventory.stop();
	}

	@Autowired
	InventoryClient client;

	/** Lewat port: @Retryable membungkus adapter dalam proxy. */
	@Autowired
	StockCheck adapter;

	@Autowired
	CircuitBreakerRegistry breakers;

	@BeforeEach
	void reset() {
		inventory.resetAll();
		breakers.circuitBreaker("inventory").reset();
	}

	@Configuration(proxyBeanMethods = false)
	@ImportAutoConfiguration({ JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
			HttpClientAutoConfiguration.class, ImperativeHttpClientAutoConfiguration.class,
			HttpServiceClientPropertiesAutoConfiguration.class, RestClientAutoConfiguration.class,
			HttpServiceClientAutoConfiguration.class })
	@EnableConfigurationProperties(InventoryProps.class)
	@ComponentScan(basePackages = "com.tokoku.order.infrastructure",
			includeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
					pattern = ".*(HttpClientsConfig|ResilienceConfig|InventoryStockAdapter)"),
			useDefaultFilters = false)
	static class HttpOnly {

		@Bean
		MeterRegistry meterRegistry() {
			return new SimpleMeterRegistry();
		}

	}

}
