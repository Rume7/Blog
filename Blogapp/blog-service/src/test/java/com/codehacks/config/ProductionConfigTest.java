package com.codehacks.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "jwt.secret=testSecretKeyForTestingPurposesOnlyThisShouldBeAtLeast256BitsLong",
    "app.magic-link.base-url=http://localhost:3000",
    "app.email-service.base-url=http://localhost:8081"
})
class ProductionConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private CacheConfig cacheConfig;



    @Test
    void shouldLoadProductionConfiguration() {
        assertThat(securityConfig).isNotNull();
        assertThat(cacheConfig).isNotNull();
    }

    @Test
    void shouldHaveSecuritySettings() {
        // Verify that security configuration is loaded
        assertThat(securityConfig).isInstanceOf(SecurityConfig.class);
    }

    @Test
    void shouldHaveCacheSettings() {
        // Verify that cache configuration is loaded
        assertThat(cacheConfig).isInstanceOf(CacheConfig.class);
    }


} 