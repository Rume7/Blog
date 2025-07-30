package com.codehacks.post.controller;

import com.codehacks.post.model.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@Testcontainers
class PostControllerIntegrationTest {

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

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "/api/v1/posts";
    }



    // ============================================================================
    // Public Endpoint Tests (No Authentication Required)
    // ============================================================================
    
    @Test
    void getAllPublishedPosts_shouldReturnPublishedPostsOnly() {
        // Given & When: Request all published posts
        ResponseEntity<Post[]> response = restTemplate.getForEntity(baseUrl(), Post[].class);
        
        // Then: Should return empty array
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getClapCount_shouldReturnCount() {
        // Given: Non-existent post ID
        
        // When: Request clap count for non-existent post
        ResponseEntity<Long> response = restTemplate.getForEntity(baseUrl() + "/999/claps/count", Long.class);
        
        // Then: Should return 0 claps
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(0L);
    }

    @Test
    void getPostById_shouldReturnNotFound_forNonExistentPost() {
        // Given: Non-existent post ID
        
        // When: Request non-existent post
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl() + "/999", String.class);
        
        // Then: Should return 404 with proper error message
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Post not found with ID: 999");
    }

} 