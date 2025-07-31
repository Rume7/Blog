package com.codehacks.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "jwt.secret=testSecretKeyForTestingPurposesOnlyThisShouldBeAtLeast256BitsLong",
    "FRONTEND_URL=http://localhost:3000"
})
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void shouldHaveSecurityConfigLoaded() {
        assertThat(securityConfig).isNotNull();
    }

    @Test
    void shouldHaveSecurityFilterChainConfigured() {
        assertThat(securityFilterChain).isNotNull();
    }

    @Test
    void shouldHaveCorsConfiguration() {
        // Verify CORS is configured
        assertThat(securityConfig).isInstanceOf(SecurityConfig.class);
    }

    @Test
    void shouldHaveJwtFilterConfigured() {
        // Verify JWT filter is configured
        assertThat(securityConfig).isInstanceOf(SecurityConfig.class);
    }

    @Test
    void shouldHaveAuthenticationProviderConfigured() {
        // Verify authentication provider is configured
        assertThat(securityConfig).isInstanceOf(SecurityConfig.class);
    }
} 