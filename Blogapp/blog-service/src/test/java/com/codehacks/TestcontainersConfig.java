package com.codehacks;

import com.codehacks.email.client.EmailServiceClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.mock;

/**
 * Testcontainers configuration for integration tests
 * Provides PostgreSQL and Redis containers for comprehensive testing
 */
@TestConfiguration
@Testcontainers
public class TestcontainersConfig {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.8-alpine")
            .withDatabaseName("blog_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        // Redis configuration
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.data.redis.enabled", () -> "true");
        
        // JWT configuration for tests
        registry.add("jwt.secret", () -> "testSecretKeyForTestingPurposesOnlyThisShouldBeAtLeast256BitsLong");
        registry.add("jwt.expiration", () -> "86400000");
        
        // Magic link configuration for tests
        registry.add("app.magic-link.base-url", () -> "http://localhost:3000");
        registry.add("app.magic-link.expiration-minutes", () -> "15");
        
        // Email service configuration for tests
        registry.add("app.email-service.base-url", () -> "http://localhost:8081");
    }
    
    /**
     * Provides a mock EmailServiceClient for integration tests
     */
    @Bean
    @Primary
    public EmailServiceClient emailServiceClient() {
        return mock(EmailServiceClient.class);
    }
} 