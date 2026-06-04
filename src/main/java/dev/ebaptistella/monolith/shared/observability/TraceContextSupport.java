package dev.ebaptistella.monolith.shared.observability;

import io.sentry.SentryEvent;

import java.util.Map;
import java.util.Optional;

import org.slf4j.MDC;

/**
 * Correlates logs and external reports with the active Micrometer/OpenTelemetry trace.
 * {@code traceId} and {@code spanId} are populated in MDC by {@code micrometer-tracing-bridge-otel}
 * whenever a span is active (HTTP, Rabbit listener/publish with observation enabled, {@code @Observed}).
 */
public final class TraceContextSupport {

    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String TRACEPARENT_HEADER = "traceparent";

    private TraceContextSupport() {
    }

    public static Optional<String> traceId() {
        return optionalMdc(TRACE_ID);
    }

    public static Optional<String> spanId() {
        return optionalMdc(SPAN_ID);
    }

    public static void enrich(SentryEvent event) {
        traceId().ifPresent(id -> {
            event.setTag(TRACE_ID, id);
            event.setExtra(TRACE_ID, id);
        });
        spanId().ifPresent(id -> {
            event.setTag(SPAN_ID, id);
            event.setExtra(SPAN_ID, id);
        });
    }

    public static void enrich(SentryEvent event, Map<String, Object> messagingHeaders) {
        enrich(event);
        if (messagingHeaders == null || messagingHeaders.isEmpty()) {
            return;
        }
        Object traceparent = messagingHeaders.get(TRACEPARENT_HEADER);
        if (traceparent != null) {
            event.setExtra(TRACEPARENT_HEADER, traceparent.toString());
        }
    }

    private static Optional<String> optionalMdc(String key) {
        return Optional.ofNullable(MDC.get(key)).filter(value -> !value.isBlank());
    }
}
