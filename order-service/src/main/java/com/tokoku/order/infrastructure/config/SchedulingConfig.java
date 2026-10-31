package com.tokoku.order.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Tanpa ini, @Scheduled pada OutboxRelay tidak pernah jalan dan outbox menumpuk. */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
class SchedulingConfig {

}
