package com.tokoku.order.infrastructure.config;

import java.time.Duration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;

/**
 * Resilience4j core tanpa integrasi Boot (slide 5): registry dibuat manual dan metric-nya di-binding ke
 * Micrometer, sehingga state breaker terlihat di Grafana (resilience4j_circuitbreaker_state).
 */
@Configuration(proxyBeanMethods = false)
@EnableResilientMethods // @Retryable dan @ConcurrencyLimit Spring Framework 7
class ResilienceConfig {

	@Bean
	CircuitBreakerRegistry circuitBreakerRegistry(MeterRegistry meterRegistry) {
		// Titik awal dari slide 82. Turunkan angkanya dari data Anda sendiri.
		CircuitBreakerConfig config = CircuitBreakerConfig.custom()
			.slidingWindowSize(20)
			.minimumNumberOfCalls(10) // default 100!
			.failureRateThreshold(50)
			.waitDurationInOpenState(Duration.ofSeconds(10))
			.permittedNumberOfCallsInHalfOpenState(3)
			.build();
		CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
		TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry).bindTo(meterRegistry);
		return registry;
	}

}
