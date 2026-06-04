package dev.ebaptistella.monolith.shared.observability;

import io.sentry.SentryEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class TraceContextSupportTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void enrichAddsActiveTraceFromMdc() {
        MDC.put(TraceContextSupport.TRACE_ID, "abc123");
        MDC.put(TraceContextSupport.SPAN_ID, "def456");

        SentryEvent event = new SentryEvent();
        TraceContextSupport.enrich(event);

        assertThat(event.getTags()).containsEntry(TraceContextSupport.TRACE_ID, "abc123");
        assertThat(event.getTags()).containsEntry(TraceContextSupport.SPAN_ID, "def456");
    }

    @Test
    void enrichAddsTraceparentFromMessagingHeaders() {
        SentryEvent event = new SentryEvent();
        TraceContextSupport.enrich(event, Map.of(TraceContextSupport.TRACEPARENT_HEADER, "00-abc-def-01"));

        assertThat(event.getExtras()).containsEntry(TraceContextSupport.TRACEPARENT_HEADER, "00-abc-def-01");
    }
}
