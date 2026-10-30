package com.tokoku.order.infrastructure.adapters.out.http;

import java.time.Duration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.tokoku.order.domain.model.Availability;
import com.tokoku.order.domain.port.out.StockCheck;
import com.tokoku.order.infrastructure.config.InventoryProps;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import org.springframework.web.client.ResourceAccessException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Jalur gagal downstream (slide 101): inventory (WireMock) lambat 6 s, read-timeout 5 s, lalu fallback saat
 * breaker OPEN. Context minimal: hanya HTTP client + adapter, tanpa database atau Kafka.
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

	@Test
	void slowInventoryTimesOutAfterReadTimeout() {
		inventory.stubFor(get(urlPathMatching("/api/inventory/.*")).willReturn(aResponse().withFixedDelay(6000)
			.withHeader("Content-Type", "application/json")
			.withBody("{\"productId\": 1, \"available\": 10}")));

		long start = System.nanoTime();
		assertThatExceptionOfType(ResourceAccessException.class).isThrownBy(() -> client.stock(1));
		Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

		assertThat(elapsed).isBetween(Duration.ofMillis(4900), Duration.ofMillis(5900));
	}

	@Test
	void openBreakerFallsBackToUnknownWithoutCallingInventory() {
		breakers.circuitBreaker("inventory").transitionToOpenState();

		assertThat(adapter.check(1, 1)).isEqualTo(Availability.UNKNOWN);
		inventory.verify(0, getRequestedFor(urlPathMatching("/api/inventory/.*")));
	}

	@Test
	void healthyInventoryAnswersAvailability() {
		inventory.stubFor(get(urlPathMatching("/api/inventory/1")).willReturn(aResponse()
			.withHeader("Content-Type", "application/json")
			.withBody("{\"productId\": 1, \"available\": 1}")));

		assertThat(adapter.check(1, 1)).isEqualTo(Availability.AVAILABLE);
		assertThat(adapter.check(1, 2)).isEqualTo(Availability.INSUFFICIENT);
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
