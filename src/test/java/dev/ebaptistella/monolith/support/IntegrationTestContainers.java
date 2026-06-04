package dev.ebaptistella.monolith.support;

import com.redis.testcontainers.RedisContainer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class IntegrationTestContainers {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:16-alpine");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7-alpine");
    private static final DockerImageName RABBIT_IMAGE = DockerImageName.parse("rabbitmq:3-management-alpine");

    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName("monolith")
            .withUsername("monolith")
            .withPassword("monolith")
            .withReuse(true);

    protected static final RedisContainer REDIS = new RedisContainer(REDIS_IMAGE)
            .withReuse(true);

    protected static final RabbitMQContainer RABBIT = new RabbitMQContainer(RABBIT_IMAGE)
            .withReuse(true);

    static {
        POSTGRES.start();
        REDIS.start();
        RABBIT.start();
    }

    protected static void registerInfrastructure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(REDIS.getFirstMappedPort()));
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", () -> String.valueOf(RABBIT.getAmqpPort()));
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
    }

    @DynamicPropertySource
    static void bindInfrastructure(DynamicPropertyRegistry registry) {
        registerInfrastructure(registry);
    }
}
