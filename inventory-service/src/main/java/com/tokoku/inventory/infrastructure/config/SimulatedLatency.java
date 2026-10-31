package com.tokoku.inventory.infrastructure.config;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

/** Menahan setiap request API selama delay yang diatur (awal dari property, bisa diubah lewat admin API). */
@Component
public class SimulatedLatency {

	private static final Logger log = LoggerFactory.getLogger(SimulatedLatency.class);

	private final AtomicReference<Duration> delay;

	private final SimulationProps props;

	SimulatedLatency(SimulationProps props) {
		this.props = props;
		this.delay = new AtomicReference<>(props.simulatedDelay());
	}

	public Duration get() {
		return delay.get();
	}

	public void set(Duration value) {
		delay.set(value);
		log.warn("Simulated delay set to {}", value);
	}

	public void apply() {
		Duration current = delay.get();
		if (props.slowRatio() > 0 && ThreadLocalRandom.current().nextDouble() < props.slowRatio()) {
			current = current.plus(props.slowDelay());
		}
		if (current.isPositive()) {
			try {
				Thread.sleep(current);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}

}
