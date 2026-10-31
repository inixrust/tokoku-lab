package com.tokoku.inventory.infrastructure.adapters.in.rest;

import com.tokoku.inventory.application.StockService;
import com.tokoku.inventory.infrastructure.config.SimulatedLatency;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
class InventoryController {

	private final StockService stocks;

	private final SimulatedLatency latency;

	InventoryController(StockService stocks, SimulatedLatency latency) {
		this.stocks = stocks;
		this.latency = latency;
	}

	@GetMapping("/{productId}")
	StockResponse stock(@PathVariable long productId) {
		latency.apply();
		return StockResponse.from(stocks.get(productId));
	}

}
