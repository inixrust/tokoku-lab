package com.tokoku.inventory.infrastructure.config;

import io.micrometer.observation.ObservationPredicate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.support.ScheduledTaskObservationContext;

/**
 * Relay outbox berjalan setiap 500 ms: tanpa predicate ini Tempo dibanjiri trace "task outboxRelay.relay" kosong,
 * dan trace request yang dicari tenggelam. Event yang dikirim relay tetap membawa trace request asal (traceparent).
 */
@Configuration(proxyBeanMethods = false)
class ObservationConfig {

	@Bean
	ObservationPredicate noScheduledTaskObservations() {
		return (name, context) -> !(context instanceof ScheduledTaskObservationContext);
	}

}
