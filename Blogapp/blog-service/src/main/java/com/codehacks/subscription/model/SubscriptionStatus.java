package com.codehacks.subscription.model;

/**
 * Enum representing the status of a subscription
 */
public enum SubscriptionStatus {
    PENDING,    // Subscription created but email not verified
    ACTIVE,     // Email verified and subscription is active
    INACTIVE,   // Subscription is inactive (user unsubscribed)
    SUSPENDED   // Subscription suspended due to bounces or complaints
} 