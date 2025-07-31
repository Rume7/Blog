package com.codehacks.subscription.service;

import com.codehacks.subscription.model.Subscription;

/**
 * Service interface for sending email notifications to subscribers
 */
public interface EmailNotificationService {

    /**
     * Send verification email to new subscriber
     */
    void sendVerificationEmail(Subscription subscription);

    /**
     * Send welcome email after verification
     */
    void sendWelcomeEmail(Subscription subscription);

    /**
     * Send unsubscribe confirmation email
     */
    void sendUnsubscribeConfirmationEmail(Subscription subscription);

    /**
     * Send new post notification
     */
    void sendNewPostNotification(Subscription subscription, String postTitle, String postUrl);

    /**
     * Send daily digest email
     */
    void sendDailyDigest(Subscription subscription, String digestContent);

    /**
     * Send weekly digest email
     */
    void sendWeeklyDigest(Subscription subscription, String digestContent);
} 