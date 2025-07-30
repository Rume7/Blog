package com.codehacks;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests
 * Provides Testcontainers PostgreSQL database and mock email service
 */
@SpringBootTest
@Import({TestConfig.class, TestcontainersConfig.class})
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {
    // Common test utilities and configurations can be added here
} 