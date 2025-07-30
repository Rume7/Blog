package com.codehacks.auth;

import com.codehacks.auth.service.AuthService;
import com.codehacks.user.dto.UserCreateRequest;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.codehacks.user.repository.UserRepository;
import com.codehacks.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@Testcontainers
class AuthIntegrationTest {

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
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserCreateRequest validRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        validRequest = new UserCreateRequest();
        validRequest.setUsername("integrationuser");
        validRequest.setFirstName("Integration");
        validRequest.setLastName("Test");
        validRequest.setEmail("integration@example.com");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void registerUser_shouldCreateUserInDatabase() {
        // When
        User registeredUser = authService.registerUser(validRequest);

        // Then
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getId()).isNotNull();
        assertThat(registeredUser.getDisplayName()).isEqualTo("integrationuser"); // Use getDisplayName() for username field
        assertThat(registeredUser.getEmail()).isEqualTo("integration@example.com");
        assertThat(registeredUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(registeredUser.getPassword()).isNull(); // Password should be null for magic link auth

        // Verify user is actually saved in database
        Optional<User> foundUser = userService.findUserById(registeredUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getDisplayName()).isEqualTo("integrationuser");
    }

    @Test
    void registerUser_shouldPreventDuplicateEmail() {
        // Given
        UserCreateRequest duplicateRequest = new UserCreateRequest();
        duplicateRequest.setUsername("differentuser");
        duplicateRequest.setFirstName("Different");
        duplicateRequest.setLastName("User");
        duplicateRequest.setEmail("integration@example.com"); // Same email

        // Register first user
        authService.registerUser(validRequest);

        // When & Then
        assertThatThrownBy(() -> authService.registerUser(duplicateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with this email already exists.");

        // Verify only one user exists
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void registerUser_shouldPreventDuplicateUsername() {
        // Given
        User user1 = new User();
        user1.setUsername("integrationuser");
        user1.setFirstName("Integration");
        user1.setLastName("User");
        user1.setEmail("integration1@email.com");
        user1.setRole(UserRole.USER);

        User user2 = new User();
        user2.setUsername("integrationuser"); // Same username
        user2.setFirstName("Integration");
        user2.setLastName("User2");
        user2.setEmail("integration2@email.com");
        user2.setRole(UserRole.USER);

        // When & Then
        userService.saveUser(user1);
        assertThatThrownBy(() -> userService.saveUser(user2))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void verifyMagicLinkAndLogin_shouldThrowException_whenUserNotFound() {
        // Given
        String token = "valid-token-for-nonexistent-user";

        // When & Then
        assertThatThrownBy(() -> authService.verifyMagicLinkAndLogin(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired magic link token");
    }

    @Test
    void userService_shouldLoadUserByUsername_afterRegistration() {
        // Given
        User registeredUser = authService.registerUser(validRequest);

        // When
        UserDetails userDetails = userService.loadUserByUsername(registeredUser.getEmail());

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(registeredUser.getEmail());
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void userService_shouldThrowException_whenUserNotFound() {
        // When & Then
        assertThatThrownBy(() -> userService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void multipleUserRegistrations_shouldWorkCorrectly() {
        // Given
        UserCreateRequest request1 = new UserCreateRequest();
        request1.setUsername("user1");
        request1.setFirstName("User");
        request1.setLastName("One");
        request1.setEmail("user1@example.com");

        UserCreateRequest request2 = new UserCreateRequest();
        request2.setUsername("user2");
        request2.setFirstName("User");
        request2.setLastName("Two");
        request2.setEmail("user2@example.com");

        // When
        User user1 = authService.registerUser(request1);
        User user2 = authService.registerUser(request2);

        // Then
        assertThat(user1).isNotNull();
        assertThat(user2).isNotNull();
        assertThat(user1.getId()).isNotEqualTo(user2.getId());
        assertThat(userRepository.count()).isEqualTo(2);

        // Verify both users can be found
        Optional<User> foundUser1 = userService.findUserById(user1.getId());
        Optional<User> foundUser2 = userService.findUserById(user2.getId());
        assertThat(foundUser1).isPresent();
        assertThat(foundUser2).isPresent();
        assertThat(foundUser1.get().getEmail()).isEqualTo("user1@example.com");
        assertThat(foundUser2.get().getEmail()).isEqualTo("user2@example.com");
    }

    @Test
    void userRegistration_shouldHandleSpecialCharactersInNames() {
        // Given
        UserCreateRequest specialRequest = new UserCreateRequest();
        specialRequest.setUsername("special_user");
        specialRequest.setFirstName("José");
        specialRequest.setLastName("O'Connor");
        specialRequest.setEmail("jose@example.com");

        // When
        User registeredUser = authService.registerUser(specialRequest);

        // Then
        assertThat(registeredUser.getFirstName()).isEqualTo("José");
        assertThat(registeredUser.getLastName()).isEqualTo("O'Connor");
        assertThat(registeredUser.getDisplayName()).isEqualTo("special_user"); // Use getDisplayName() for username field
    }

    @Test
    void userRegistration_shouldHandleLongNames() {
        // Given
        UserCreateRequest longNameRequest = new UserCreateRequest();
        longNameRequest.setUsername("longnameuser");
        longNameRequest.setFirstName("A".repeat(50)); // Maximum allowed length
        longNameRequest.setLastName("B".repeat(50)); // Maximum allowed length
        longNameRequest.setEmail("longname@example.com");

        // When
        User registeredUser = authService.registerUser(longNameRequest);

        // Then
        assertThat(registeredUser.getFirstName()).isEqualTo("A".repeat(50));
        assertThat(registeredUser.getLastName()).isEqualTo("B".repeat(50));
    }
} 