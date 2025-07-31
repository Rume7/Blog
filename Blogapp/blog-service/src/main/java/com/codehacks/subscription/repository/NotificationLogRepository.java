package com.codehacks.subscription.repository;

import com.codehacks.subscription.model.NotificationLog;
import com.codehacks.subscription.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /**
     * Find notification logs by subscription ID
     */
    List<NotificationLog> findBySubscriptionIdOrderByCreatedAtDesc(Long subscriptionId);

    /**
     * Find notification logs by status
     */
    List<NotificationLog> findByStatus(NotificationStatus status);

    /**
     * Find notification logs by email
     */
    List<NotificationLog> findByEmailOrderByCreatedAtDesc(String email);

    /**
     * Find notification logs created after a specific time
     */
    List<NotificationLog> findByCreatedAtAfter(LocalDateTime since);

    /**
     * Find failed notifications
     */
    List<NotificationLog> findByStatusIn(List<NotificationStatus> statuses);

    /**
     * Count notifications by status
     */
    long countByStatus(NotificationStatus status);

    /**
     * Find notification logs for a specific post
     */
    List<NotificationLog> findByPostIdOrderByCreatedAtDesc(Long postId);

    /**
     * Find notification logs that need to be retried (failed notifications)
     */
    @Query("SELECT nl FROM NotificationLog nl WHERE nl.status IN ('FAILED', 'PENDING') " +
           "AND nl.createdAt > :since")
    List<NotificationLog> findNotificationsForRetry(@Param("since") LocalDateTime since);
} 