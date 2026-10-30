package com.tokoku.product.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class StartupInfo implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(StartupInfo.class);

	private final InventoryProps inventory;

	StartupInfo(InventoryProps inventory) {
		this.inventory = inventory;
	}

	@Override
	public void run(ApplicationArguments args) {
		log.info("tokoku.inventory: {}", inventory);
	}

}
