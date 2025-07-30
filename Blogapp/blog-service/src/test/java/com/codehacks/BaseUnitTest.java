package com.codehacks;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for unit tests
 * Provides mock configurations without Testcontainers
 */
@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseUnitTest {
    // Common test utilities and configurations can be added here
} 