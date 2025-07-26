package com.codehacks.user;

import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.codehacks.user.repository.UserRepository;
import com.codehacks.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@ExtendWith(SpringExtension.class)
class UserIntegrationTest {

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
        registry.add("jwt.secret", () -> "testSecretKeyForTestingPurposesOnlyThisShouldBeAtLeast256BitsLong");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_andFindById() {
        // Given
        User user = new User();
        user.setUsername("integration");
        user.setFirstName("Integration");
        user.setLastName("Test");
        user.setEmail("integration@email.com");
        user.setRole(UserRole.USER);

        // When
        User created = userService.saveUser(user);
        Optional<User> found = userService.findUserById(created.getId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getDisplayName()).isEqualTo("integration"); // Use getDisplayName() for username field
        assertThat(found.get().getEmail()).isEqualTo("integration@email.com");
    }

    @Test
    void findByEmail_shouldReturnUser() {
        // Given
        User user = new User();
        user.setUsername("emailtest");
        user.setFirstName("Email");
        user.setLastName("Test");
        user.setEmail("emailtest@example.com");
        user.setRole(UserRole.USER);
        User savedUser = userService.saveUser(user);

        // When
        Optional<User> found = userService.findByEmail("emailtest@example.com");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedUser.getId());
        assertThat(found.get().getEmail()).isEqualTo("emailtest@example.com");
    }

    @Test
    void findByUsername_shouldReturnUser() {
        // Given
        User user = new User();
        user.setUsername("usernametest");
        user.setFirstName("Username");
        user.setLastName("Test");
        user.setEmail("usernametest@example.com");
        user.setRole(UserRole.USER);
        User savedUser = userService.saveUser(user);

        // When
        Optional<User> found = userService.findByUsername("usernametest");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedUser.getId());
        assertThat(found.get().getDisplayName()).isEqualTo("usernametest"); // Use getDisplayName() for username field
    }

    @Test
    void findAllUsers_shouldReturnAllUsers() {
        // Given
        User user1 = new User();
        user1.setUsername("user1");
        user1.setFirstName("User");
        user1.setLastName("One");
        user1.setEmail("user1@example.com");
        user1.setRole(UserRole.USER);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setEmail("user2@example.com");
        user2.setRole(UserRole.ADMIN);

        userService.saveUser(user1);
        userService.saveUser(user2);

        // When
        List<User> allUsers = userService.findAllUsers();
        
        // Then
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting("displayName").contains("user1", "user2"); // Use displayName for username field
    }

    @Test
    void deleteUser_shouldRemoveUser() {
        // Given
        User user = new User();
        user.setUsername("todelete");
        user.setFirstName("To");
        user.setLastName("Delete");
        user.setEmail("todelete@example.com");
        user.setRole(UserRole.USER);
        User savedUser = userService.saveUser(user);

        // When
        userService.deleteUser(savedUser.getId());
        
        // Then
        Optional<User> found = userService.findUserById(savedUser.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails() {
        // Given
        User user = new User();
        user.setUsername("springuser");
        user.setFirstName("Spring");
        user.setLastName("User");
        user.setEmail("springuser@example.com");
        user.setRole(UserRole.USER);
        userService.saveUser(user);

        // When
        UserDetails userDetails = userService.loadUserByUsername("springuser@example.com");
        
        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("springuser@example.com");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> 
            userService.loadUserByUsername("nonexistent@example.com"));
    }

    @Test
    void duplicateEmail_shouldBePrevented_byDatabaseConstraint() {
        // Note: This test verifies that the database prevents duplicate emails
        // The database has a unique constraint on email field
        
        // Given
        User user1 = new User();
        user1.setUsername("user1");
        user1.setFirstName("User");
        user1.setLastName("One");
        user1.setEmail("duplicate@example.com");
        user1.setRole(UserRole.USER);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setEmail("duplicate@example.com"); // Same email
        user2.setRole(UserRole.USER);

        // When
        User saved1 = userService.saveUser(user1);
        
        // Then - second user should fail due to unique constraint
        assertThat(saved1.getId()).isNotNull();
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
            userService.saveUser(user2);
        });
    }
} 