package com.tokoku.product.infrastructure.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Menyambungkan appender OTEL di logback-spring.xml ke SDK OpenTelemetry milik Spring Boot. Boot 4.1 menyediakan
 * exporter log (management.opentelemetry.logging.export.otlp.endpoint), tetapi tidak memasang appender Logback.
 */
@Configuration(proxyBeanMethods = false)
class OpenTelemetryLogsConfig {

	@Bean
	InitializingBean installOpenTelemetryAppender(OpenTelemetry openTelemetry) {
		return () -> OpenTelemetryAppender.install(openTelemetry);
	}

}
