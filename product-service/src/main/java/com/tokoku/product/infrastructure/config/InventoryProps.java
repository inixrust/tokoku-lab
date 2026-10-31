package com.tokoku.product.infrastructure.config;

import java.time.Duration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration type-safe untuk akses ke inventory-service (slide 24).
 * {@code @Validated}: bila property kritis hilang, aplikasi gagal start (fail fast).
 */
@Validated
@ConfigurationProperties("tokoku.inventory")
public record InventoryProps(
		@NotBlank String baseUrl,
		@NotNull Duration readTimeout,
		@DefaultValue("3") @Min(1) int maxAttempts) {
}
