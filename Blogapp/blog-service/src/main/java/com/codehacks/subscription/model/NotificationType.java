package com.codehacks.subscription.model;

/**
 * Enum representing the type of notification preference
 */
public enum NotificationType {
    INSTANT,    // Send notification immediately when new post is published
    DAILY,      // Send daily digest of new posts
    WEEKLY,     // Send weekly digest of new posts
    NONE        // No notifications (for unsubscribed users)
} 