package dev.ebaptistella.monolith.config.modulith;

import io.micrometer.common.KeyValues;
import org.springframework.modulith.observability.support.ModulithContext;
import org.springframework.modulith.observability.support.ModulithObservationConvention;

/**
 * Workaround for Spring Modulith 1.4.x: {@code DefaultModulithObservationConvention} omits
 * {@code module.invocation-type} on API invocations but adds it for event listeners. Prometheus
 * requires the same tag keys on all series for a meter name.
 */
final class ModulithMetricsConfig {

    private static final String API_INVOCATION_TYPE = "api";
    private static final String MODULE_KEY = "module.key";
    private static final String INVOCATION_TYPE_KEY = "module.invocation-type";
    private static final String MODULE_METHOD_KEY = "module.method";

    private ModulithMetricsConfig() {
    }

    static ModulithObservationConvention consistentModulithObservationConvention() {
        return new ConsistentModulithObservationConvention();
    }

    private static boolean isEventListener(ModulithContext context) {
        try {
            return context.getModule().isEventListenerInvocation(context.getInvocation());
        } catch (Exception ex) {
            return false;
        }
    }

    private static final class ConsistentModulithObservationConvention implements ModulithObservationConvention {

        @Override
        public String getName() {
            return "module.requests";
        }

        @Override
        public String getContextualName(ModulithContext context) {
            return "[" + context.getApplicationName() + "] " + context.getModule().getDisplayName();
        }

        @Override
        public KeyValues getLowCardinalityKeyValues(ModulithContext context) {
            KeyValues keyValues = KeyValues.of(
                    io.micrometer.common.KeyValue.of(MODULE_KEY, context.getModule().getIdentifier().toString()));
            String invocationType = isEventListener(context) ? "event-listener" : API_INVOCATION_TYPE;
            return keyValues.and(io.micrometer.common.KeyValue.of(INVOCATION_TYPE_KEY, invocationType));
        }

        @Override
        public KeyValues getHighCardinalityKeyValues(ModulithContext context) {
            return KeyValues.of(io.micrometer.common.KeyValue.of(
                    MODULE_METHOD_KEY, context.getInvocation().getMethod().getName()));
        }
    }
}
