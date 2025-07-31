package com.codehacks.subscription.controller;

import com.codehacks.subscription.dto.SubscriptionRequest;
import com.codehacks.subscription.dto.SubscriptionResponse;
import com.codehacks.subscription.dto.SubscriptionStatistics;
import com.codehacks.subscription.model.NotificationType;
import com.codehacks.subscription.service.SubscriptionService;
import com.codehacks.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(Constants.SUBSCRIPTIONS_PATH)
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Subscribe to blog notifications
     */
    @PostMapping
    public ResponseEntity<?> subscribe(@Valid @RequestBody SubscriptionRequest request) {
        try {
            log.info("Subscription request received for email: {}", request.getEmail());
            SubscriptionResponse response = subscriptionService.createSubscription(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Subscription failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Subscription failed with unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Subscription failed: " + e.getMessage());
        }
    }

    /**
     * Verify subscription by token
     */
    @GetMapping("/verify/{token}")
    public ResponseEntity<?> verifySubscription(@PathVariable String token) {
        try {
            log.info("Verification request received for token: {}", token);
            SubscriptionResponse response = subscriptionService.verifySubscription(token);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Verification failed with unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Verification failed: " + e.getMessage());
        }
    }

    /**
     * Unsubscribe by token
     */
    @DeleteMapping("/{token}")
    public ResponseEntity<?> unsubscribe(@PathVariable String token) {
        try {
            log.info("Unsubscribe request received for token: {}", token);
            subscriptionService.unsubscribe(token);
            return ResponseEntity.ok("Successfully unsubscribed");
        } catch (IllegalArgumentException e) {
            log.warn("Unsubscribe failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unsubscribe failed with unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unsubscribe failed: " + e.getMessage());
        }
    }

    /**
     * Update subscription preferences
     */
    @PutMapping("/{token}/preferences")
    public ResponseEntity<?> updatePreferences(
            @PathVariable String token,
            @RequestParam NotificationType notificationType) {
        try {
            log.info("Preferences update request received for token: {}", token);
            SubscriptionResponse response = subscriptionService.updatePreferences(token, notificationType);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Preferences update failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Preferences update failed with unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Preferences update failed: " + e.getMessage());
        }
    }

    /**
     * Get subscription by email (admin only)
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSubscriptionByEmail(@PathVariable String email) {
        try {
            log.info("Get subscription request received for email: {}", email);
            return subscriptionService.getSubscriptionByEmail(email)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Get subscription failed with unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get subscription: " + e.getMessage());
        }
    }

    /**
     * Get subscription by token
     */
    @GetMapping("/{token}")
    public ResponseEntity<?> getSubscriptionByToken(@PathVariable String token) {
        try {
            log.info("Get subscription request received for token: {}", token);
            return subscriptionService.getSubscriptionByToken(token)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Get subscription failed with unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get subscription: " + e.getMessage());
        }
    }

    /**
     * Get all active subscriptions (admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionResponse>> getAllActiveSubscriptions() {
        try {
            log.info("Get all active subscriptions request received");
            List<SubscriptionResponse> subscriptions = subscriptionService.getActiveSubscriptions();
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            log.error("Get all subscriptions failed with unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get subscription statistics (admin only)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionStatistics> getStatistics() {
        try {
            log.info("Get subscription statistics request received");
            SubscriptionStatistics statistics = subscriptionService.getStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Get statistics failed with unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 