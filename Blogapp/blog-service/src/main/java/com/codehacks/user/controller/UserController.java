package com.codehacks.user.controller;

import com.codehacks.user.dto.UserResponse;
import com.codehacks.user.dto.UserUpdateRequest;
import com.codehacks.user.model.User;
import com.codehacks.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No authenticated user found.");
        }

        // authentication.getName() returns the 'username' from UserDetails, which is the email in our setup
        String authenticatedUserEmail = authentication.getName();

        Optional<User> userOptional = userService.findByEmail(authenticatedUserEmail);

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(UserResponse.fromUser(userOptional.get())); // Return UserResponse
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Authenticated user not found in database.");
        }
    }

    // Endpoint to get a user by ID
    // Accessible by ADMIN or the user themselves
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        // Check if user is admin or the user themselves
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required.");
        }
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            // For non-admin users, check if they're accessing their own profile
            String authenticatedUserEmail = authentication.getName();
            Optional<User> authenticatedUser = userService.findByEmail(authenticatedUserEmail);
            if (authenticatedUser.isEmpty() || !authenticatedUser.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
            }
        }
        
        Optional<User> userOptional = userService.findUserById(id);
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(UserResponse.fromUser(userOptional.get())); // Return UserResponse
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + id);
        }
    }

    // Endpoint to update a user's profile
    // Accessible by ADMIN or the user themselves
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        // Check if user is admin or the user themselves
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required.");
        }
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            // For non-admin users, check if they're updating their own profile
            String authenticatedUserEmail = authentication.getName();
            Optional<User> authenticatedUser = userService.findByEmail(authenticatedUserEmail);
            if (authenticatedUser.isEmpty() || !authenticatedUser.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
            }
        }
        
        Optional<User> existingUserOptional = userService.findUserById(id);

        if (existingUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + id);
        }

        User existingUser = existingUserOptional.get();

        // Update fields from the DTO
        existingUser.setUsername(request.getUsername());
        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());

        // Handle email change:
        if (!existingUser.getEmail().equals(request.getEmail())) {
            if (userService.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Email is already taken by another user.");
            }
            existingUser.setEmail(request.getEmail());
        }

        User updatedUser = userService.saveUser(existingUser);
        return ResponseEntity.ok(UserResponse.fromUser(updatedUser)); // Return UserResponse
    }

    // Endpoint to get all users
    // Only accessible by ADMIN
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        Optional<User> userOptional = userService.findUserById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + id);
        }
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully.");
    }
}
