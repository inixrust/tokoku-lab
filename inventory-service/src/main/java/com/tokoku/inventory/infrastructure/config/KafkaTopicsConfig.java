package com.tokoku.inventory.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Topic yang diproduksi inventory-service. Dibuat otomatis oleh KafkaAdmin saat startup. */
@Configuration(proxyBeanMethods = false)
class KafkaTopicsConfig {

	@Bean
	NewTopic stockEventsTopic() {
		return TopicBuilder.name("stock-events").partitions(3).replicas(1).build();
	}

}
