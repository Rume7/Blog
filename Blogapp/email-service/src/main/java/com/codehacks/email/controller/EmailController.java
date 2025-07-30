package com.codehacks.email.controller;

import com.codehacks.email.dto.MagicLinkEmailRequest;
import com.codehacks.email.dto.MagicLinkEmailResponse;
import com.codehacks.email.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;

    /**
     * Sends a magic link email to the specified user
     */
    @PostMapping("/magic-link")
    public ResponseEntity<MagicLinkEmailResponse> sendMagicLinkEmail(
            @Valid @RequestBody MagicLinkEmailRequest request) {
        try {
            log.info("Magic link email request received for: {}", request.email());
            MagicLinkEmailResponse response = emailService.sendMagicLinkEmail(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send magic link email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MagicLinkEmailResponse(
                            request.email(),
                            "Failed to send magic link email: " + e.getMessage(),
                            null
                    ));
        }
    }

    /**
     * Validates a magic link token and returns true if valid
     */
    @GetMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        try {
            log.info("Token validation request received");
            boolean isValid = emailService.validateMagicLinkToken(token);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("Failed to validate token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    /**
     * Retrieves the email address associated with a magic link token
     */
    @GetMapping("/email-from-token")
    public ResponseEntity<String> getEmailFromToken(@RequestParam String token) {
        try {
            log.info("Get email from token request received");
            String email = emailService.getEmailFromToken(token);
            return ResponseEntity.ok(email);
        } catch (Exception e) {
            log.error("Failed to get email from token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Health check endpoint for the email service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Email Service is running");
    }
} 