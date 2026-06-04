package dev.ebaptistella.monolith.config.modulith;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.modulith.observability.support.ModulithObservationConvention;

/**
 * Beans de observabilidade Modulith em configuração separada com factory methods estáticos,
 * evitando inicialização prematura do {@link ObservationRegistry} no bootstrap.
 */
@Configuration
public class ModulithObservationConfiguration {

    @Bean
    static ModulithObservationConvention modulithObservationConvention() {
        return ModulithMetricsConfig.consistentModulithObservationConvention();
    }

    @Bean
    static ModulithObservationInterceptorPatcher modulithObservationInterceptorPatcher(
            ModulithObservationConvention modulithObservationConvention,
            ObjectProvider<ObservationRegistry> observationRegistryProvider,
            Environment environment) {
        return new ModulithObservationInterceptorPatcher(
                modulithObservationConvention, observationRegistryProvider, environment);
    }
}
