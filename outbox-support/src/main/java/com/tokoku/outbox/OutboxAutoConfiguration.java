package com.tokoku.outbox;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.autoconfigure.JdbcClientAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Mendaftarkan komponen outbox. Butuh tabel {@code outbox} dan {@code processed_event} di schema service;
 * lihat {@code outbox-support/README.md}.
 */
@AutoConfiguration(after = { JdbcClientAutoConfiguration.class, KafkaAutoConfiguration.class })
@ConditionalOnBean(JdbcClient.class)
public class OutboxAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	OutboxWriter outboxWriter(JdbcClient jdbc) {
		return new OutboxWriter(jdbc);
	}

	@Bean
	@ConditionalOnMissingBean
	ProcessedEvents processedEvents(JdbcClient jdbc) {
		return new ProcessedEvents(jdbc);
	}

	@Configuration(proxyBeanMethods = false)
	@EnableScheduling
	@ConditionalOnBean(KafkaTemplate.class)
	@ConditionalOnProperty(name = "tokoku.outbox.relay-enabled", matchIfMissing = true)
	static class RelayConfiguration {

		@Bean
		@ConditionalOnMissingBean
		OutboxRelay outboxRelay(JdbcClient jdbc, KafkaTemplate<String, String> kafka) {
			return new OutboxRelay(jdbc, kafka, 100);
		}

	}

}
