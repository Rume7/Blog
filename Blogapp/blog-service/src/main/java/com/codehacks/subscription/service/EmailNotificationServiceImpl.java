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
    public void sendWelcomeEmail(Subscription subscription) {
        try {
            log.info("Sending welcome email to: {}", subscription.getEmail());

            // Use the new subscription welcome email endpoint
            emailServiceClient.sendSubscriptionWelcomeEmail(subscription.getEmail(), baseUrl);
            
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

            // For now, use a simple text email since we don't have a template for this
            // In a production system, you'd want to create a proper template
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

    // Content building methods
    private String buildWelcomeEmailContent(Subscription subscription) {
        return String.format("""
            Hello there,
            
            🎉 Congratulations! Your subscription to our newsletter has been successfully verified!
            
            You're now part of our community and will receive our weekly newsletter with the latest blog posts, insights, and updates.
            
            What to expect:
            - Weekly digest of our latest blog posts
            - Exclusive content and insights
            - Tips and best practices
            - Community highlights and updates
            
            Your first newsletter will arrive in your inbox soon. In the meantime, feel free to explore our blog!
            
            Best regards,
            The %s Team
            """, 
            blogName
        );
    }

    private String buildUnsubscribeEmailContent(Subscription subscription) {
        return String.format("""
            Hello there,
            
            You have been successfully unsubscribed from our newsletter.
            
            We're sorry to see you go! If you change your mind, you can always subscribe again by visiting our blog.
            
            Best regards,
            The %s Team
            """, 
            blogName
        );
    }

    private String buildNewPostNotificationContent(Subscription subscription, String postTitle, String postUrl) {
        return String.format("""
            Hello there,
            
            We just published a new blog post that we think you'll enjoy!
            
            Title: %s
            
            Read it here: %s
            
            Happy reading!
            
            Best regards,
            The %s Team
            """, 
            postTitle,
            postUrl,
            blogName
        );
    }

    private String buildDigestEmailContent(Subscription subscription, String digestType, String digestContent) {
        return String.format("""
            Hello there,
            
            Here's your %s digest from %s:
            
            %s
            
            Happy reading!
            
            Best regards,
            The %s Team
            """, 
            digestType,
            blogName,
            digestContent,
            blogName
        );
    }
} 