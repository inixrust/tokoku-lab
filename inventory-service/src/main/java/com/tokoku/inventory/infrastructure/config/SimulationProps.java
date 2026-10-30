package com.tokoku.inventory.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Pengaturan simulasi untuk lab: inventory yang lambat. */
@ConfigurationProperties("inventory")
public record SimulationProps(@DefaultValue("0s") Duration simulatedDelay) {
}
