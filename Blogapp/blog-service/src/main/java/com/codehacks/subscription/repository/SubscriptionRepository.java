package com.codehacks.subscription.repository;

import com.codehacks.subscription.model.NotificationType;
import com.codehacks.subscription.model.Subscription;
import com.codehacks.subscription.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Find subscription by email
     */
    Optional<Subscription> findByEmail(String email);

    /**
     * Find subscription by token
     */
    Optional<Subscription> findByToken(String token);

    /**
     * Find active subscriptions by notification type
     */
    List<Subscription> findByStatusAndNotificationTypeAndActiveTrue(
            SubscriptionStatus status, 
            NotificationType notificationType
    );

    /**
     * Find subscriptions that haven't received notifications since a specific time
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = :status " +
           "AND s.notificationType = :notificationType " +
           "AND s.active = true " +
           "AND (s.lastNotificationSent IS NULL OR s.lastNotificationSent < :since)")
    List<Subscription> findActiveSubscriptionsForNotification(
            @Param("status") SubscriptionStatus status,
            @Param("notificationType") NotificationType notificationType,
            @Param("since") LocalDateTime since
    );

    /**
     * Check if email is already subscribed
     */
    boolean existsByEmail(String email);

    /**
     * Find all active subscriptions
     */
    List<Subscription> findByStatusAndActiveTrue(SubscriptionStatus status);

    /**
     * Count active subscriptions
     */
    long countByStatusAndActiveTrue(SubscriptionStatus status);
} 