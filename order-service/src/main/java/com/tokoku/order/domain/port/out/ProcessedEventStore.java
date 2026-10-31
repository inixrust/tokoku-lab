package com.tokoku.order.domain.port.out;

import java.util.UUID;

/** Dedup untuk consumer idempotent (slide 61). */
public interface ProcessedEventStore {

	/** @return {@code true} bila event baru; {@code false} bila duplikat */
	boolean markIfNew(UUID eventId);

}
