package com.tokoku.order.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Topic yang diproduksi order-service. Dibuat otomatis oleh KafkaAdmin saat startup. */
@Configuration(proxyBeanMethods = false)
class KafkaTopicsConfig {

	@Bean
	NewTopic ordersTopic() {
		return TopicBuilder.name("orders").partitions(3).replicas(1).build();
	}

}
