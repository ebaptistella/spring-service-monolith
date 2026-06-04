package dev.ebaptistella.monolith.modules.customer.diplomat.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ebaptistella.monolith.modules.customer.models.Customer;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class CustomerCacheConfiguration {

    @Bean
    RedisCacheManagerBuilderCustomizer customerCacheCustomizer(ObjectMapper objectMapper) {
        Jackson2JsonRedisSerializer<Customer> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Customer.class);
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return builder -> builder.withCacheConfiguration(CustomerCacheNames.CUSTOMERS, configuration);
    }
}
