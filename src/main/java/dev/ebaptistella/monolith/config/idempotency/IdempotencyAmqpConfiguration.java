package dev.ebaptistella.monolith.config.idempotency;

import dev.ebaptistella.monolith.shared.idempotency.IdempotencyHeaders;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class IdempotencyAmqpConfiguration {

    @Bean
    BeanPostProcessor rabbitTemplateIdempotencyHeaderPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof RabbitTemplate template) {
                    MessagePostProcessor processor = message -> {
                        Object key = message.getMessageProperties().getHeaders().get("idempotencyKey");
                        if (key != null) {
                            message.getMessageProperties()
                                    .setHeader(IdempotencyHeaders.X_IDEMPOTENCY_KEY, key.toString());
                        }
                        return message;
                    };
                    template.addBeforePublishPostProcessors(processor);
                }
                return bean;
            }
        };
    }
}
