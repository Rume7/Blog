package com.codehacks;

import com.codehacks.email.client.EmailServiceClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;

/**
 * Testcontainers configuration for integration tests
 * Provides PostgreSQL and Redis containers for comprehensive testing
 */
@TestConfiguration
@Testcontainers
public class TestcontainersConfig {

    @Container
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
        // PostgreSQL configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
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
        EmailServiceClient mockClient = mock(EmailServiceClient.class);
        
        // Configure mock behavior for magic link operations
        when(mockClient.validateMagicLinkToken("invalid-token")).thenReturn(false);
        when(mockClient.validateMagicLinkToken("token-for-nonexistent-user")).thenReturn(true);
        when(mockClient.getEmailFromToken("token-for-nonexistent-user")).thenReturn("nonexistent@example.com");
        
        // Configure mock behavior for valid tokens
        when(mockClient.validateMagicLinkToken("valid-token")).thenReturn(true);
        when(mockClient.getEmailFromToken("valid-token")).thenReturn("test@example.com");
        
        // Configure mock behavior for sendMagicLinkEmail - do nothing (void method)
        doNothing().when(mockClient).sendMagicLinkEmail(any());
        
        return mockClient;
    }
} 