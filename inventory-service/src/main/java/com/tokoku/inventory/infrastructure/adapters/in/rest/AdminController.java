package com.tokoku.inventory.infrastructure.adapters.in.rest;

import java.util.Map;

import com.tokoku.inventory.infrastructure.config.SimulatedLatency;

import org.springframework.boot.convert.DurationStyle;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Khusus lab: mengubah latency tanpa restart. Contoh: PUT /api/admin/simulated-delay?value=8s */
@RestController
@RequestMapping("/api/admin/simulated-delay")
class AdminController {

	private final SimulatedLatency latency;

	AdminController(SimulatedLatency latency) {
		this.latency = latency;
	}

	@GetMapping
	Map<String, String> get() {
		return Map.of("simulatedDelay", latency.get().toString());
	}

	@PutMapping
	Map<String, String> set(@RequestParam String value) {
		// Format sama dengan property: "8s", "500ms", atau ISO-8601 "PT8S".
		latency.set(DurationStyle.detectAndParse(value));
		return get();
	}

}
