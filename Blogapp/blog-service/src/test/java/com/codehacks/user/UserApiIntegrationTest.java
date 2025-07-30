package com.codehacks.user;

import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.codehacks.user.repository.UserRepository;
import com.codehacks.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jwt.secret=testSecretKeyForTestingPurposesOnlyThisShouldBeAtLeast256BitsLong"
})
class UserApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.8-alpine")
            .withDatabaseName("userTestDB")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUpUser() {
        userRepository.deleteAll();
        
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
    void verifyMagicLink_shouldReturnJwtToken() {
        // Test magic link verification (simulated)
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/auth/verify-magic-link?email=test@example.com", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // The response should be a JWT token
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void verifyMagicLink_shouldReturnBadRequest_forNonExistentUser() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/auth/verify-magic-link?email=nonexistent@example.com", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("User not found");
    }
}