package com.tokoku.product.infrastructure.config;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/** Membuktikan fail fast tanpa database: hanya binding configuration yang diuji. */
class InventoryPropsTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withUserConfiguration(PropsConfig.class);

	@Test
	void bindsWithDefaultMaxAttempts() {
		contextRunner
			.withPropertyValues("tokoku.inventory.base-url=http://inventory:8080",
					"tokoku.inventory.read-timeout=2s")
			.run(context -> {
				InventoryProps props = context.getBean(InventoryProps.class);
				assertThat(props.baseUrl()).isEqualTo("http://inventory:8080");
				assertThat(props.readTimeout()).isEqualTo(Duration.ofSeconds(2));
				assertThat(props.maxAttempts()).isEqualTo(3);
			});
	}

	@Test
	void failsToStartWithoutBaseUrl() {
		contextRunner
			.withPropertyValues("tokoku.inventory.read-timeout=2s")
			.run(context -> assertThat(context).hasFailed()
				.getFailure()
				.rootCause()
				.hasMessageContaining("baseUrl"));
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(InventoryProps.class)
	static class PropsConfig {

	}

}
