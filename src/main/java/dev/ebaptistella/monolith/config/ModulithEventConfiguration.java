package dev.ebaptistella.monolith.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.modulith.events.Externalized;

@Configuration
class ModulithEventConfiguration {

    @Bean
    EventExternalizationConfiguration integrationEventsExternalization() {
        return EventExternalizationConfiguration.externalizing()
                .selectByAnnotation(Externalized.class)
                .build();
    }
}
