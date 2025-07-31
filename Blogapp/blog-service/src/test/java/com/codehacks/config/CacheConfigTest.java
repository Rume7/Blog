package com.codehacks.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class CacheConfigTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.cache.type", () -> "redis");
    }

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldHaveCacheManagerConfigured() {
        assertThat(cacheManager).isNotNull();
    }

    @Test
    void shouldHaveRequiredCaches() {
        assertThat(cacheManager.getCache("users")).isNotNull();
        assertThat(cacheManager.getCache("posts")).isNotNull();
        assertThat(cacheManager.getCache("claps")).isNotNull();
        assertThat(cacheManager.getCache("auth")).isNotNull();
    }

    @Test
    void shouldBeAbleToUseCache() {
        // Test basic cache functionality
        var cache = cacheManager.getCache("users");
        assertThat(cache).isNotNull();
        
        // Test cache operations
        cache.put("test-key", "test-value");
        var retrieved = cache.get("test-key");
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.get()).isEqualTo("test-value");
    }
} 