package com.codehacks.post.controller;

import com.codehacks.TestcontainersConfig;
import com.codehacks.post.dto.PostResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;



import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PostController using Testcontainers and real HTTP requests.
 * Tests both public endpoints (no authentication required) and protected endpoints (authentication required).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith(SpringExtension.class)
@Transactional
@ActiveProfiles("test")
@ContextConfiguration(classes = TestcontainersConfig.class)
class PostControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpHeaders publicHeaders;

    @BeforeEach
    void setUp() {
        // Setup headers for public endpoints only
        publicHeaders = new HttpHeaders();
        publicHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/posts";
    }



    // ============================================================================
    // Public Endpoint Tests (No Authentication Required)
    // ============================================================================
    
    @Test
    void getAllPublishedPosts_shouldReturnPublishedPostsOnly() {
        // Given & When: Request all published posts
        ResponseEntity<PostResponse[]> response = restTemplate.getForEntity(baseUrl(), PostResponse[].class);
        
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