package com.tokoku.order.infrastructure.config;

import com.tokoku.order.infrastructure.adapters.out.http.InventoryClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * Satu grup per service tujuan (slide 50). Builder RestClient untuk grup dibuat Spring,
 * jadi timeout global spring.http.clients.* ikut berlaku.
 */
@Configuration(proxyBeanMethods = false)
@ImportHttpServices(group = "inventory", types = InventoryClient.class)
class HttpClientsConfig {

	@Bean
	RestClientHttpServiceGroupConfigurer inventoryGroup(InventoryProps props) {
		return groups -> groups.filterByName("inventory")
			.forEachClient((group, builder) -> builder.baseUrl(props.baseUrl()));
	}

}
