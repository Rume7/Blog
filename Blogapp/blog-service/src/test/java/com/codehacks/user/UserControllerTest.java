package com.codehacks.user;

import com.codehacks.user.controller.UserController;
import com.codehacks.user.dto.UserResponse;
import com.codehacks.user.dto.UserUpdateRequest;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.codehacks.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Mock
    private UserService userService;
    
    @InjectMocks
    private UserController userController;

    private UserResponse sampleResponse;
    private UserUpdateRequest sampleUpdateRequest;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("testuser");
        sampleUser.setFirstName("Test");
        sampleUser.setLastName("User");
        sampleUser.setEmail("test@example.com");
        sampleUser.setRole(UserRole.USER);
        
        sampleResponse = UserResponse.fromUser(sampleUser);
        
        sampleUpdateRequest = new UserUpdateRequest();
        sampleUpdateRequest.setUsername("updateduser");
        sampleUpdateRequest.setFirstName("Updated");
        sampleUpdateRequest.setLastName("User");
        sampleUpdateRequest.setEmail("updated@example.com");
    }

    private void setupAdminAuthentication() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAllUsers_shouldReturnList() {
        // Given
        when(userService.findAllUsers()).thenReturn(List.of(sampleUser));

        // When
        ResponseEntity<List<UserResponse>> result = userController.getAllUsers();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
        assertThat(result.getBody().get(0).getUsername()).isEqualTo("testuser");
        verify(userService).findAllUsers();
    }

    @Test
    void getUserById_shouldReturnUserIfFound() {
        // Given
        when(userService.findUserById(1L)).thenReturn(Optional.of(sampleUser));
        setupAdminAuthentication();

        // When
        ResponseEntity<?> result = userController.getUserById(1L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponse response = (UserResponse) result.getBody();
        assertThat(response.getUsername()).isEqualTo("testuser");
        verify(userService).findUserById(1L);
    }

    @Test
    void getUserById_shouldReturnNotFoundIfMissing() {
        // Given
        when(userService.findUserById(1L)).thenReturn(Optional.empty());
        setupAdminAuthentication();

        // When
        ResponseEntity<?> result = userController.getUserById(1L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isEqualTo("User not found with ID: 1");
        verify(userService).findUserById(1L);
    }

    @Test
    void updateUser_shouldReturnUpdatedUserIfFound() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setRole(UserRole.USER);
        
        when(userService.findUserById(1L)).thenReturn(Optional.of(sampleUser));
        when(userService.findByEmail("updated@example.com")).thenReturn(Optional.empty());
        when(userService.saveUser(any(User.class))).thenReturn(updatedUser);
        setupAdminAuthentication();

        // When
        ResponseEntity<?> result = userController.updateUser(1L, sampleUpdateRequest);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponse response = (UserResponse) result.getBody();
        assertThat(response.getUsername()).isEqualTo("updateduser");
        verify(userService).findUserById(1L);
        verify(userService).saveUser(any(User.class));
    }

    @Test
    void updateUser_shouldReturnNotFoundIfMissing() {
        // Given
        when(userService.findUserById(1L)).thenReturn(Optional.empty());
        setupAdminAuthentication();

        // When
        ResponseEntity<?> result = userController.updateUser(1L, sampleUpdateRequest);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isEqualTo("User not found with ID: 1");
        verify(userService).findUserById(1L);
    }

    @Test
    void updateUser_shouldReturnBadRequestIfEmailAlreadyTaken() {
        // Given
        User existingUserWithEmail = new User();
        existingUserWithEmail.setId(2L);
        existingUserWithEmail.setEmail("updated@example.com");
        
        when(userService.findUserById(1L)).thenReturn(Optional.of(sampleUser));
        when(userService.findByEmail("updated@example.com")).thenReturn(Optional.of(existingUserWithEmail));
        setupAdminAuthentication();

        // When
        ResponseEntity<?> result = userController.updateUser(1L, sampleUpdateRequest);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isEqualTo("Email is already taken by another user.");
        verify(userService).findUserById(1L);
        verify(userService).findByEmail("updated@example.com");
    }

    @Test
    void deleteUser_shouldReturnSuccessMessage() {
        // Given
        when(userService.findUserById(1L)).thenReturn(Optional.of(sampleUser));

        // When
        ResponseEntity<String> result = userController.deleteUser(1L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo("User deleted successfully.");
        verify(userService).findUserById(1L);
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_shouldReturnNotFoundIfUserMissing() {
        // Given
        when(userService.findUserById(1L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<String> result = userController.deleteUser(1L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isEqualTo("User not found with ID: 1");
        verify(userService).findUserById(1L);
    }

    @Test
    void getCurrentUser_shouldReturnUserIfAuthenticated() {
        // Given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        
        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        // When
        ResponseEntity<?> result = userController.getCurrentUser();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponse response = (UserResponse) result.getBody();
        assertThat(response.getUsername()).isEqualTo("testuser");
        verify(userService).findByEmail("test@example.com");
    }

    @Test
    void getCurrentUser_shouldReturnUnauthorizedIfNotAuthenticated() {
        // Given
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // When
        ResponseEntity<?> result = userController.getCurrentUser();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.getBody()).isEqualTo("No authenticated user found.");
    }

    @Test
    void getCurrentUser_shouldReturnNotFoundIfUserNotInDatabase() {
        // Given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> result = userController.getCurrentUser();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isEqualTo("Authenticated user not found in database.");
        verify(userService).findByEmail("nonexistent@example.com");
    }
} 