package com.codehacks.subscription.service;

import com.codehacks.subscription.dto.SubscriptionRequest;
import com.codehacks.subscription.dto.SubscriptionResponse;
import com.codehacks.subscription.dto.SubscriptionStatistics;
import com.codehacks.subscription.model.NotificationLog;
import com.codehacks.subscription.model.NotificationStatus;
import com.codehacks.subscription.model.NotificationType;
import com.codehacks.subscription.model.Subscription;
import com.codehacks.subscription.model.SubscriptionStatus;
import com.codehacks.subscription.repository.NotificationLogRepository;
import com.codehacks.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final EmailNotificationService emailNotificationService;

    /**
     * Create a new subscription
     */
    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        log.info("Creating subscription for email: {}", request.getEmail());

        // Check if email is already subscribed
        if (subscriptionRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already subscribed: " + request.getEmail());
        }

        // Create new subscription
        Subscription subscription = Subscription.builder()
                .email(request.getEmail())
                .notificationType(request.getNotificationType())
                .status(SubscriptionStatus.PENDING)
                .emailVerified(false)
                .active(true)
                .build();

        subscription = subscriptionRepository.save(subscription);

        // Send verification email
        emailNotificationService.sendVerificationEmail(subscription);

        log.info("Subscription created successfully for email: {}", request.getEmail());
        return SubscriptionResponse.fromSubscription(subscription);
    }

    /**
     * Verify subscription by token
     */
    public SubscriptionResponse verifySubscription(String token) {
        log.info("Verifying subscription with token: {}", token);

        Optional<Subscription> subscriptionOpt = subscriptionRepository.findByToken(token);
        if (subscriptionOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid verification token");
        }

        Subscription subscription = subscriptionOpt.get();
        
        if (subscription.isEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }

        // Update subscription status
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setEmailVerified(true);
        subscription.setVerifiedAt(LocalDateTime.now());
        subscription.setActive(true);

        subscription = subscriptionRepository.save(subscription);

        // Send welcome email
        emailNotificationService.sendWelcomeEmail(subscription);

        log.info("Subscription verified successfully for email: {}", subscription.getEmail());
        return SubscriptionResponse.fromSubscription(subscription);
    }

    /**
     * Unsubscribe by token
     */
    public void unsubscribe(String token) {
        log.info("Processing unsubscribe request for token: {}", token);

        Optional<Subscription> subscriptionOpt = subscriptionRepository.findByToken(token);
        if (subscriptionOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid unsubscribe token");
        }

        Subscription subscription = subscriptionOpt.get();
        subscription.setStatus(SubscriptionStatus.INACTIVE);
        subscription.setActive(false);
        subscription.setNotificationType(NotificationType.NONE);

        subscriptionRepository.save(subscription);

        // Send unsubscribe confirmation email
        emailNotificationService.sendUnsubscribeConfirmationEmail(subscription);

        log.info("Unsubscribed successfully for email: {}", subscription.getEmail());
    }

    /**
     * Update subscription preferences
     */
    public SubscriptionResponse updatePreferences(String token, NotificationType notificationType) {
        log.info("Updating preferences for token: {}", token);

        Optional<Subscription> subscriptionOpt = subscriptionRepository.findByToken(token);
        if (subscriptionOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid token");
        }

        Subscription subscription = subscriptionOpt.get();
        subscription.setNotificationType(notificationType);
        subscription = subscriptionRepository.save(subscription);

        log.info("Preferences updated successfully for email: {}", subscription.getEmail());
        return SubscriptionResponse.fromSubscription(subscription);
    }

    /**
     * Get subscription by email
     */
    @Transactional(readOnly = true)
    public Optional<SubscriptionResponse> getSubscriptionByEmail(String email) {
        return subscriptionRepository.findByEmail(email)
                .map(SubscriptionResponse::fromSubscription);
    }

    /**
     * Get subscription by token
     */
    @Transactional(readOnly = true)
    public Optional<SubscriptionResponse> getSubscriptionByToken(String token) {
        return subscriptionRepository.findByToken(token)
                .map(SubscriptionResponse::fromSubscription);
    }

    /**
     * Get all active subscriptions
     */
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getActiveSubscriptions() {
        return subscriptionRepository.findByStatusAndActiveTrue(SubscriptionStatus.ACTIVE)
                .stream()
                .map(SubscriptionResponse::fromSubscription)
                .collect(Collectors.toList());
    }

    /**
     * Get subscriptions for instant notification
     */
    @Transactional(readOnly = true)
    public List<Subscription> getSubscriptionsForInstantNotification() {
        return subscriptionRepository.findByStatusAndNotificationTypeAndActiveTrue(
                SubscriptionStatus.ACTIVE, 
                NotificationType.INSTANT
        );
    }

    /**
     * Get subscriptions for daily digest
     */
    @Transactional(readOnly = true)
    public List<Subscription> getSubscriptionsForDailyDigest() {
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        return subscriptionRepository.findActiveSubscriptionsForNotification(
                SubscriptionStatus.ACTIVE, 
                NotificationType.DAILY, 
                since
        );
    }

    /**
     * Get subscriptions for weekly digest
     */
    @Transactional(readOnly = true)
    public List<Subscription> getSubscriptionsForWeeklyDigest() {
        LocalDateTime since = LocalDateTime.now().minusWeeks(1);
        return subscriptionRepository.findActiveSubscriptionsForNotification(
                SubscriptionStatus.ACTIVE, 
                NotificationType.WEEKLY, 
                since
        );
    }

    /**
     * Log notification sent
     */
    public void logNotificationSent(Subscription subscription, String subject, String content, Long postId) {
        NotificationLog log = NotificationLog.builder()
                .subscription(subscription)
                .email(subscription.getEmail())
                .notificationType(subscription.getNotificationType())
                .subject(subject)
                .content(content)
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now())
                .postId(postId)
                .build();

        notificationLogRepository.save(log);

        // Update last notification sent timestamp
        subscription.setLastNotificationSent(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }

    /**
     * Log notification failure
     */
    public void logNotificationFailure(Subscription subscription, String subject, String content, String errorMessage, Long postId) {
        NotificationLog log = NotificationLog.builder()
                .subscription(subscription)
                .email(subscription.getEmail())
                .notificationType(subscription.getNotificationType())
                .subject(subject)
                .content(content)
                .status(NotificationStatus.FAILED)
                .errorMessage(errorMessage)
                .postId(postId)
                .build();

        notificationLogRepository.save(log);
    }

    /**
     * Get subscription statistics
     */
    @Transactional(readOnly = true)
    public SubscriptionStatistics getStatistics() {
        long totalActive = subscriptionRepository.countByStatusAndActiveTrue(SubscriptionStatus.ACTIVE);
        long totalPending = subscriptionRepository.countByStatusAndActiveTrue(SubscriptionStatus.PENDING);
        long totalInactive = subscriptionRepository.countByStatusAndActiveTrue(SubscriptionStatus.INACTIVE);

        return SubscriptionStatistics.builder()
                .totalActive(totalActive)
                .totalPending(totalPending)
                .totalInactive(totalInactive)
                .totalSubscriptions(totalActive + totalPending + totalInactive)
                .build();
    }
} 