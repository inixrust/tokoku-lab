package com.tokoku.order.infrastructure.adapters.in.rest.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Tanpa customerId: identitas pemanggil selalu diambil dari token (slide 91). */
public record PlaceOrderRequest(@NotEmpty @Size(max = 20) List<@Valid Line> lines) {

	public record Line(@Positive long productId, @Min(1) @Max(100) int quantity) {
	}

}
