package dev.ebaptistella.monolith.config.observability;

import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;
import liquibase.Liquibase;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.integration.spring.Customizer;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Spans para Redis (Lettuce), Liquibase (startup) e propriedades AMQP/SMTP em {@code application-observability.yml}.
 */
@Configuration
@Profile("observability")
@AutoConfigureBefore({RedisAutoConfiguration.class, LiquibaseAutoConfiguration.class})
@ConditionalOnClass({MicrometerTracing.class, SpringLiquibase.class})
public class InfrastructureObservabilityConfiguration {

    @Bean
    static ClientResourcesBuilderCustomizer redisObservationCustomizer(
            ObjectProvider<ObservationRegistry> observationRegistryProvider) {
        return builder -> builder.tracing(
                new MicrometerTracing(observationRegistryProvider.getObject(), "redis"));
    }

    @Bean
    static BeanPostProcessor liquibaseObservationCustomizer(
            ObjectProvider<ObservationRegistry> observationRegistryProvider) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName)
                    throws BeansException {
                if (!(bean instanceof SpringLiquibase springLiquibase)) {
                    return bean;
                }
                Customizer<Liquibase> existing = springLiquibase.getCustomizer();
                springLiquibase.setCustomizer(liquibase -> {
                    if (existing != null) {
                        existing.customize(liquibase);
                    }
                    ChangeExecListener delegate = liquibase.getDefaultChangeExecListener();
                    liquibase.setChangeExecListener(new ChainingLiquibaseChangeExecListener(
                            new ObservingLiquibaseChangeExecListener(observationRegistryProvider.getObject()),
                            delegate));
                });
                return bean;
            }
        };
    }
}
