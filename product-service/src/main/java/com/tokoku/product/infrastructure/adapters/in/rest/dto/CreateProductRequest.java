package com.tokoku.product.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
		@NotBlank @Size(max = 40) String sku,
		@NotBlank @Size(max = 120) String name,
		@NotNull @Positive BigDecimal price) {
}
