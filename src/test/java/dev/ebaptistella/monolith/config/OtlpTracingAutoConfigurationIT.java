package dev.ebaptistella.monolith.config;

import dev.ebaptistella.monolith.MonolithApplication;
import dev.ebaptistella.monolith.support.IntegrationTestContainers;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest(
        classes = MonolithApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "observability"})
class OtlpTracingAutoConfigurationIT extends IntegrationTestContainers {

    @Autowired
    ApplicationContext context;

    @Autowired
    Environment environment;

    @Test
    void otlpGrpcExporterIsRegisteredWithObservabilityProfile() {
        assertThat(environment.getProperty("management.otlp.tracing.export.enabled")).isEqualTo("true");
        assertThat(environment.getProperty("management.otlp.tracing.transport")).isEqualTo("grpc");
        assertThat(context.getBeansOfType(OtlpGrpcSpanExporter.class)).isNotEmpty();
    }
}
