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