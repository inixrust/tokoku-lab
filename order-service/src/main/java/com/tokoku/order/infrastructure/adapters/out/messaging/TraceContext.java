package com.tokoku.order.infrastructure.adapters.out.messaging;

import java.util.HashMap;
import java.util.Map;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;

import org.springframework.stereotype.Component;

/** Mengambil header W3C traceparent dari span yang sedang aktif. */
@Component
class TraceContext {

	private final Tracer tracer;

	private final Propagator propagator;

	TraceContext(Tracer tracer, Propagator propagator) {
		this.tracer = tracer;
		this.propagator = propagator;
	}

	String currentTraceparent() {
		Span span = tracer.currentSpan();
		if (span == null) {
			return null;
		}
		Map<String, String> headers = new HashMap<>();
		propagator.inject(span.context(), headers, Map::put);
		return headers.get("traceparent");
	}

}
