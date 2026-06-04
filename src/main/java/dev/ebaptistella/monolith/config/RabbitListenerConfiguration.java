package dev.ebaptistella.monolith.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ebaptistella.monolith.config.rabbit.RabbitListenerRetryProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
@RequiredArgsConstructor
class RabbitListenerConfiguration {

    private final RabbitListenerRetryProperties retryProperties;

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            ObjectMapper objectMapper,
            RetryOperationsInterceptor rabbitRetryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter(objectMapper));
        factory.setDefaultRequeueRejected(false);
        factory.setErrorHandler(new ConditionalRejectingErrorHandler());
        if (retryProperties.enabled()) {
            factory.setAdviceChain(rabbitRetryInterceptor);
        }
        return factory;
    }

    @Bean
    SimpleRabbitListenerContainerFactory deadLetterListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    RetryOperationsInterceptor rabbitRetryInterceptor(MessageRecoverer rabbitMessageRecoverer) {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(retryProperties.maxAttempts())
                .backOffOptions(
                        retryProperties.initialInterval().toMillis(),
                        retryProperties.multiplier(),
                        retryProperties.maxInterval().toMillis())
                .recoverer(rabbitMessageRecoverer)
                .build();
    }

    @Bean
    MessageRecoverer rabbitMessageRecoverer() {
        return new RejectAndDontRequeueRecoverer();
    }
}
