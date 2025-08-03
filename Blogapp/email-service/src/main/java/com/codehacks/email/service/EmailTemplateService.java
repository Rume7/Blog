package com.codehacks.email.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailTemplateService {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateService.class);

    /**
     * Generates the HTML content for magic link authentication emails
     */
    public String generateMagicLinkEmailHtmlContent(String username, String magicLinkUrl) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/magic-link-email.html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            
            return template
                    .replace("{{username}}", username != null ? username : "there")
                    .replace("{{magicLinkUrl}}", magicLinkUrl);
        } catch (IOException e) {
            log.error("Failed to load HTML email template", e);
            return generateFallbackMagicLinkEmailContent(username, magicLinkUrl);
        }
    }

    /**
     * Generates the plain text content for magic link authentication emails
     */
    public String generateMagicLinkEmailTextContent(String username, String magicLinkUrl) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/magic-link-email.txt");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            
            return template
                    .replace("{{username}}", username != null ? username : "there")
                    .replace("{{magicLinkUrl}}", magicLinkUrl);
        } catch (IOException e) {
            log.error("Failed to load text email template", e);
            return generateFallbackMagicLinkEmailContent(username, magicLinkUrl);
        }
    }

    /**
     * Generates the HTML content for subscription verification emails
     */
    public String generateSubscriptionVerificationEmailHtmlContent(String email, String verificationUrl) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/subscription-verification.html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            
            return template
                    .replace("{{email}}", email)
                    .replace("{{verificationUrl}}", verificationUrl);
        } catch (IOException e) {
            log.error("Failed to load subscription verification HTML template", e);
            return generateFallbackSubscriptionVerificationEmailContent(email, verificationUrl);
        }
    }

    /**
     * Generates the HTML content for subscription welcome emails
     */
    public String generateSubscriptionWelcomeEmailHtmlContent(String email, String blogUrl) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/subscription-welcome.html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            
            return template
                    .replace("{{email}}", email)
                    .replace("{{blogUrl}}", blogUrl != null ? blogUrl : "http://localhost:3000");
        } catch (IOException e) {
            log.error("Failed to load subscription welcome HTML template", e);
            return generateFallbackSubscriptionWelcomeEmailContent(email, blogUrl);
        }
    }

    /**
     * Fallback method for generating magic link email content
     */
    private String generateFallbackMagicLinkEmailContent(String username, String magicLinkUrl) {
        return String.format("""
            Hello %s,
            
            You requested a magic link to sign in to BlogApp. Click the link below to access your account:
            
            %s
            
            This link will expire in 15 minutes and can only be used once.
            
            If you didn't request this link, please ignore this email.
            
            Best regards,
            The BlogApp Team
            """, 
            username != null ? username : "there",
            magicLinkUrl
        );
    }

    /**
     * Fallback method for generating subscription verification email content
     */
    private String generateFallbackSubscriptionVerificationEmailContent(String email, String verificationUrl) {
        return String.format("""
            Hello there,
            
            Thank you for subscribing to our newsletter! We're excited to keep you updated with our latest blog posts and insights.
            
            To complete your subscription and start receiving our weekly newsletter, please click the link below:
            
            %s
            
            This verification link will expire in 24 hours. If you don't verify your subscription within this time, you'll need to subscribe again.
            
            If you didn't request this subscription, you can safely ignore this email.
            
            Best regards,
            The BlogApp Team
            """, 
            verificationUrl
        );
    }

    /**
     * Fallback method for generating subscription welcome email content
     */
    private String generateFallbackSubscriptionWelcomeEmailContent(String email, String blogUrl) {
        return String.format("""
            Hello there,
            
            🎉 Congratulations! Your subscription to our newsletter has been successfully verified!
            
            You're now part of our community and will receive our weekly newsletter with the latest blog posts, insights, and updates.
            
            What to expect:
            - Weekly digest of our latest blog posts
            - Exclusive content and insights
            - Tips and best practices
            - Community highlights and updates
            
            Your first newsletter will arrive in your inbox soon. In the meantime, feel free to explore our blog:
            %s
            
            If you ever want to manage your subscription preferences or unsubscribe, you can do so at any time by clicking the unsubscribe link at the bottom of our emails.
            
            Best regards,
            The BlogApp Team
            """, 
            blogUrl != null ? blogUrl : "http://localhost:3000"
        );
    }

    /**
     * Generates welcome email content for newly registered users
     */
    public String generateWelcomeEmailContent(String username) {
        return String.format("""
            Welcome to BlogApp, %s!
            
            Your account has been successfully created. You can now start creating and sharing your blog posts.
            
            We're excited to have you as part of our community!
            
            Best regards,
            The BlogApp Team
            """, 
            username
        );
    }

    /**
     * Generates password reset email content
     */
    public String generatePasswordResetEmailContent(String username, String resetLink) {
        return String.format("""
            Hello %s,
            
            You requested to reset your password for BlogApp. Click the link below to set a new password:
            
            %s
            
            This link will expire in 15 minutes and can only be used once.
            
            If you didn't request this password reset, please ignore this email.
            
            Best regards,
            The BlogApp Team
            """, 
            username,
            resetLink
        );
    }
} 