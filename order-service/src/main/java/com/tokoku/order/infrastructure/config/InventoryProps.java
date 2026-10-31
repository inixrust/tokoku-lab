package com.tokoku.order.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/** Akses ke inventory-service (pola L3.1). Timeout diatur global lewat spring.http.clients.*. */
@Validated
@ConfigurationProperties("tokoku.inventory")
public record InventoryProps(
		@NotBlank String baseUrl,
		@DefaultValue("3") @Min(1) int maxAttempts) {
}
