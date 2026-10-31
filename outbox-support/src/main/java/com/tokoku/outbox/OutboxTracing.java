package com.tokoku.outbox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;

/** Header W3C traceparent dari span aktif, untuk disimpan bersama event di outbox. */
final class OutboxTracing implements Supplier<String> {

	private final Tracer tracer;

	private final Propagator propagator;

	OutboxTracing(Tracer tracer, Propagator propagator) {
		this.tracer = tracer;
		this.propagator = propagator;
	}

	@Override
	public String get() {
		Span span = tracer.currentSpan();
		if (span == null) {
			return null;
		}
		Map<String, String> headers = new HashMap<>();
		propagator.inject(span.context(), headers, Map::put);
		return headers.get("traceparent");
	}

}
