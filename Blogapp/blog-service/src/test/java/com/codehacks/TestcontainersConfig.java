package com.codehacks;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Testcontainers configuration for integration tests
 * Provides a real PostgreSQL database in a container
 */
@TestConfiguration
public class TestcontainersConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:16.8-alpine")
                .withDatabaseName("blog_test")
                .withUsername("test_user")
                .withPassword("test_password");
    }
} 