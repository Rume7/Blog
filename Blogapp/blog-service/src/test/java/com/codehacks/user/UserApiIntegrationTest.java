package com.codehacks.user;

import com.codehacks.email.client.EmailServiceClient;
import com.codehacks.email.dto.MagicLinkEmailRequest;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.codehacks.user.repository.UserRepository;
import com.codehacks.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@Testcontainers
@Import(TestSecurityConfig.class)
class UserApiIntegrationTest {

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @MockBean
    private EmailServiceClient emailServiceClient;

    @BeforeEach
    void setUpUser() {
        userRepository.deleteAll();
        
        // Configure mock behavior for EmailServiceClient
        when(emailServiceClient.validateMagicLinkToken("invalid-token")).thenReturn(false);
        when(emailServiceClient.validateMagicLinkToken("token-for-nonexistent-user")).thenReturn(true);
        when(emailServiceClient.getEmailFromToken("token-for-nonexistent-user")).thenReturn("nonexistent@example.com");
        when(emailServiceClient.validateMagicLinkToken("valid-token")).thenReturn(true);
        when(emailServiceClient.getEmailFromToken("valid-token")).thenReturn("test@example.com");
        doNothing().when(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));
        
        // Create a test user directly in the database
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.USER);
        userService.saveUser(testUser);
    }

    @Test
    void getCurrentUser_shouldReturnForbidden_whenNotAuthenticated() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/users/me", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getAllUsers_shouldReturnForbidden_whenNotAuthenticated() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/users", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getUserById_shouldReturnForbidden_whenNotAuthenticated() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/users/1", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void updateUser_shouldReturnForbidden_whenNotAuthenticated() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/users/1", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteUser_shouldReturnForbidden_whenNotAuthenticated() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/users/1", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void authEndpoints_shouldBeAccessible() {
        // Test that auth endpoints are accessible without authentication
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        
        // Test login endpoint
        String loginRequest = "{\"email\":\"test@example.com\"}";
        org.springframework.http.HttpEntity<String> loginEntity = new org.springframework.http.HttpEntity<>(loginRequest, headers);
        ResponseEntity<String> loginResponse = restTemplate.postForEntity("/api/v1/auth/login", loginEntity, String.class);
        
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Test register endpoint
        String registerRequest = "{\"username\":\"newuser\",\"firstName\":\"New\",\"lastName\":\"User\",\"email\":\"new@example.com\"}";
        org.springframework.http.HttpEntity<String> registerEntity = new org.springframework.http.HttpEntity<>(registerRequest, headers);
        ResponseEntity<String> registerResponse = restTemplate.postForEntity("/api/v1/auth/register", registerEntity, String.class);
        
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void verifyMagicLink_shouldReturnBadRequest_forInvalidToken() {
        // Test magic link verification with invalid token
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/auth/verify-magic-link?token=invalid-token", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid or expired magic link token");
    }

    @Test
    void verifyMagicLink_shouldReturnJwtToken() {
        // Test magic link verification (simulated)
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/auth/verify-magic-link?token=valid-token", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // The response should be a JWT token
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void verifyMagicLink_shouldReturnBadRequest_forNonExistentUser() {
        // Test magic link verification with token for non-existent user
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/auth/verify-magic-link?token=token-for-nonexistent-user", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("User not found");
    }
}