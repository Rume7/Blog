package com.codehacks.subscription.service;

import com.codehacks.email.client.EmailServiceClient;
import com.codehacks.email.dto.MagicLinkEmailRequest;
import com.codehacks.subscription.model.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final EmailServiceClient emailServiceClient;

    @Value("${app.magic-link.base-url:http://localhost:3000}")
    private String baseUrl;

    @Value("${app.blog.name:Blog App}")
    private String blogName;

    @Override
    public void sendVerificationEmail(Subscription subscription) {
        try {
            log.info("Sending verification email to: {}", subscription.getEmail());

            String verificationUrl = baseUrl + "/verify-subscription?token=" + subscription.getToken();
            
            String subject = "Verify your subscription to " + blogName;
            String content = buildVerificationEmailContent(subscription, verificationUrl);

            // Use the existing email service client
            MagicLinkEmailRequest request = new MagicLinkEmailRequest(
                    subscription.getEmail(), 
                    "subscription-verification"
            );

            emailServiceClient.sendMagicLinkEmail(request);
            
            log.info("Verification email sent successfully to: {}", subscription.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", subscription.getEmail(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public void sendWelcomeEmail(Subscription subscription) {
        try {
            log.info("Sending welcome email to: {}", subscription.getEmail());

            String subject = "Welcome to " + blogName + "!";
            String content = buildWelcomeEmailContent(subscription);

            MagicLinkEmailRequest request = new MagicLinkEmailRequest(
                    subscription.getEmail(), 
                    "welcome"
            );

            emailServiceClient.sendMagicLinkEmail(request);
            
            log.info("Welcome email sent successfully to: {}", subscription.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", subscription.getEmail(), e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    @Override
    public void sendUnsubscribeConfirmationEmail(Subscription subscription) {
        try {
            log.info("Sending unsubscribe confirmation email to: {}", subscription.getEmail());

            String subject = "Unsubscribed from " + blogName;
            String content = buildUnsubscribeEmailContent(subscription);

            MagicLinkEmailRequest request = new MagicLinkEmailRequest(
                    subscription.getEmail(), 
                    "unsubscribe-confirmation"
            );

            emailServiceClient.sendMagicLinkEmail(request);
            
            log.info("Unsubscribe confirmation email sent successfully to: {}", subscription.getEmail());
        } catch (Exception e) {
            log.error("Failed to send unsubscribe confirmation email to: {}", subscription.getEmail(), e);
            throw new RuntimeException("Failed to send unsubscribe confirmation email", e);
        }
    }

    @Override
    public void sendNewPostNotification(Subscription subscription, String postTitle, String postUrl) {
        try {
            log.info("Sending new post notification to: {}", subscription.getEmail());

            String subject = "New post: " + postTitle;
            String content = buildNewPostNotificationContent(subscription, postTitle, postUrl);

            MagicLinkEmailRequest request = new MagicLinkEmailRequest(
                    subscription.getEmail(), 
                    "new-post-notification"
            );

            emailServiceClient.sendMagicLinkEmail(request);
            
            log.info("New post notification sent successfully to: {}", subscription.getEmail());
        } catch (Exception e) {
            log.error("Failed to send new post notification to: {}", subscription.getEmail(), e);
            throw new RuntimeException("Failed to send new post notification", e);
        }
    }

    @Override
    public void sendDailyDigest(Subscription subscription, String digestContent) {
        try {
            log.info("Sending daily digest to: {}", subscription.getEmail());

            String subject = "Daily Digest - " + blogName;
            String content = buildDigestEmailContent(subscription, "Daily", digestContent);

            MagicLinkEmailRequest request = new MagicLinkEmailRequest(
                    subscription.getEmail(), 
                    "daily-digest"
            );

            emailServiceClient.sendMagicLinkEmail(request);
            
            log.info("Daily digest sent successfully to: {}", subscription.getEmail());
        } catch (Exception e) {
            log.error("Failed to send daily digest to: {}", subscription.getEmail(), e);
            throw new RuntimeException("Failed to send daily digest", e);
        }
    }

    @Override
    public void sendWeeklyDigest(Subscription subscription, String digestContent) {
        try {
            log.info("Sending weekly digest to: {}", subscription.getEmail());

            String subject = "Weekly Digest - " + blogName;
            String content = buildDigestEmailContent(subscription, "Weekly", digestContent);

            MagicLinkEmailRequest request = new MagicLinkEmailRequest(
                    subscription.getEmail(), 
                    "weekly-digest"
            );

            emailServiceClient.sendMagicLinkEmail(request);
            
            log.info("Weekly digest sent successfully to: {}", subscription.getEmail());
        } catch (Exception e) {
            log.error("Failed to send weekly digest to: {}", subscription.getEmail(), e);
            throw new RuntimeException("Failed to send weekly digest", e);
        }
    }

    private String buildVerificationEmailContent(Subscription subscription, String verificationUrl) {
        return String.format("""
            <html>
            <body>
                <h2>Welcome to %s!</h2>
                <p>Hi there,</p>
                <p>Thank you for subscribing to our blog! To complete your subscription and start receiving updates, please click the link below:</p>
                <p><a href="%s">Verify Your Subscription</a></p>
                <p>If you didn't request this subscription, you can safely ignore this email.</p>
                <p>Best regards,<br>The %s Team</p>
            </body>
            </html>
            """, blogName, verificationUrl, blogName);
    }

    private String buildWelcomeEmailContent(Subscription subscription) {
        return String.format("""
            <html>
            <body>
                <h2>Welcome to %s!</h2>
                <p>Hi there,</p>
                <p>Your subscription has been successfully verified! You're now subscribed to receive updates from our blog.</p>
                <p>You'll receive notifications based on your preference: <strong>%s</strong></p>
                <p>If you ever want to change your preferences or unsubscribe, you can do so by clicking the unsubscribe link in any of our emails.</p>
                <p>Happy reading!<br>The %s Team</p>
            </body>
            </html>
            """, blogName, subscription.getNotificationType().name(), blogName);
    }

    private String buildUnsubscribeEmailContent(Subscription subscription) {
        return String.format("""
            <html>
            <body>
                <h2>Unsubscribed Successfully</h2>
                <p>Hi there,</p>
                <p>You have been successfully unsubscribed from %s.</p>
                <p>We're sorry to see you go! If you change your mind, you can always subscribe again by visiting our website.</p>
                <p>Thank you for being part of our community!<br>The %s Team</p>
            </body>
            </html>
            """, blogName, blogName);
    }

    private String buildNewPostNotificationContent(Subscription subscription, String postTitle, String postUrl) {
        return String.format("""
            <html>
            <body>
                <h2>New Post Available!</h2>
                <p>Hi there,</p>
                <p>A new post has been published on %s:</p>
                <h3><a href="%s">%s</a></h3>
                <p>Click the link above to read the full post.</p>
                <p>Happy reading!<br>The %s Team</p>
            </body>
            </html>
            """, blogName, postUrl, postTitle, blogName);
    }

    private String buildDigestEmailContent(Subscription subscription, String digestType, String digestContent) {
        return String.format("""
            <html>
            <body>
                <h2>%s Digest - %s</h2>
                <p>Hi there,</p>
                <p>Here's your %s digest of new posts from %s:</p>
                %s
                <p>Happy reading!<br>The %s Team</p>
            </body>
            </html>
            """, digestType, blogName, digestType.toLowerCase(), blogName, digestContent, blogName);
    }
} 