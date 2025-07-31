package com.codehacks.subscription.model;

/**
 * Enum representing the status of a notification
 */
public enum NotificationStatus {
    PENDING,    // Notification queued for sending
    SENT,       // Notification successfully sent
    FAILED,     // Notification failed to send
    BOUNCED,    // Email bounced back
    COMPLAINED  // User marked as spam
} 