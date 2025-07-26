package com.codehacks.auth.controller;

import com.codehacks.auth.dto.LoginRequest;
import com.codehacks.auth.service.AuthService;
import com.codehacks.user.dto.UserCreateRequest;
import com.codehacks.user.dto.UserResponse;
import com.codehacks.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserCreateRequest request) {
        try {
            User registeredUser = authService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromUser(registeredUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed: " + e.getMessage());
        }
    }

    // Endpoint to initiate magic link login (sends email)
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        // In a real app, this would trigger sending a magic link email
        // For simulation, we'll just log it.
        System.out.println("Login initiated for: " + request.getEmail());
        // You might return a success message indicating email sent
        return ResponseEntity.ok("Magic link sent to your email (simulated).");
    }

    // Endpoint for magic link verification and actual login (user clicks link)
    @GetMapping("/verify-magic-link")
    public ResponseEntity<?> verifyMagicLink(@RequestParam String email) { // In real app: @RequestParam String token
        try {
            String jwtToken = authService.verifyMagicLinkAndLogin(email); // In real app: token
            return ResponseEntity.ok(jwtToken); // Return JWT token
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Magic link verification failed: " + e.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No authenticated user to refresh token for.");
            }

            String newToken = authService.refreshToken(authentication);
            return ResponseEntity.ok(newToken);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token refresh failed: " + e.getMessage());
        }
    }
}
