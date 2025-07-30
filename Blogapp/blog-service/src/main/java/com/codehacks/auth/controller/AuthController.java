package com.codehacks.auth.controller;

import com.codehacks.auth.dto.LoginRequest;
import com.codehacks.auth.service.AuthService;
import com.codehacks.user.dto.UserCreateRequest;
import com.codehacks.user.dto.UserResponse;
import com.codehacks.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserCreateRequest request) {
        try {
            log.info("Registration request received for email: {}", request.getEmail());
            User registeredUser = authService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromUser(registeredUser));
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Registration failed with unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Initiates magic link login process
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Magic link login request received for: {}", request.getEmail());
            authService.initiateMagicLinkLogin(request);
            return ResponseEntity.ok("Magic link sent to your email. Please check your inbox and click the link to sign in.");
        } catch (IllegalArgumentException e) {
            log.warn("Magic link login failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Magic link login failed with unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed: " + e.getMessage());
        }
    }

    /**
     * Verifies magic link token and completes login
     */
    @GetMapping("/verify-magic-link")
    public ResponseEntity<?> verifyMagicLink(@RequestParam String token) {
        try {
            log.info("Magic link verification request received");
            String jwtToken = authService.verifyMagicLinkAndLogin(token);
            return ResponseEntity.ok(jwtToken);
        } catch (IllegalArgumentException e) {
            log.warn("Magic link verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Magic link verification failed with unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Magic link verification failed: " + e.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Token refresh failed: No authenticated user");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No authenticated user to refresh token for.");
            }

            String newToken = authService.refreshToken(authentication);
            log.debug("Token refreshed successfully for user: {}", authentication.getName());
            return ResponseEntity.ok(newToken);
        } catch (Exception e) {
            log.error("Token refresh failed with unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token refresh failed: " + e.getMessage());
        }
    }
}
