package com.tokoku.order.infrastructure.adapters.out.http;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/** Contract client sebagai interface: mudah dibaca dan di-mock (slide 50). */
@HttpExchange("/api/inventory")
public interface InventoryClient {

	@GetExchange("/{productId}")
	StockResponse stock(@PathVariable long productId);

}
