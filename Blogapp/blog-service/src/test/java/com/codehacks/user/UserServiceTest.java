package com.codehacks.user;

import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.codehacks.user.repository.UserRepository;
import com.codehacks.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // Given
        String email = "test@example.com";
        User user = createTestUser(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        UserDetails result = userService.loadUserByUsername(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(email);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(email));
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findAllUsers_shouldReturnAllUsers() {
        // Given
        User user1 = createTestUser("user1@example.com");
        User user2 = createTestUser("user2@example.com");
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // When
        List<User> result = userService.findAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(user1, user2);
        verify(userRepository).findAll();
    }

    @Test
    void findUserById_shouldReturnUser_whenUserExists() {
        // Given
        Long userId = 1L;
        User user = createTestUser("test@example.com");
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findUserById(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    void findUserById_shouldReturnEmpty_whenUserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findUserById(userId);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findById(userId);
    }

    @Test
    void findByEmail_shouldReturnUser_whenUserExists() {
        // Given
        String email = "test@example.com";
        User user = createTestUser(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenUserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByUsername_shouldReturnUser_whenUserExists() {
        // Given
        String username = "testuser";
        User user = createTestUser("test@example.com");
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByUsername(username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDisplayName()).isEqualTo(username); // Use getDisplayName() for the username field
        verify(userRepository).findByUsername(username);
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenUserNotFound() {
        // Given
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByUsername(username);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByUsername(username);
    }

    @Test
    void saveUser_shouldSaveAndReturnUser() {
        // Given
        User userToSave = createTestUser("test@example.com");
        User savedUser = createTestUser("test@example.com");
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.saveUser(userToSave);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).save(userToSave);
    }

    @Test
    void deleteUser_shouldCallRepository() {
        // Given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    private User createTestUser(String email) {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setRole(UserRole.USER);
        return user;
    }
} 