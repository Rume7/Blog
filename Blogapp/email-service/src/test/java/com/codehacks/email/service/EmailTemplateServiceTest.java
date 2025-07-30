package com.codehacks.email.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceTest {

    @InjectMocks
    private EmailTemplateService emailTemplateService;

    @Test
    void generateMagicLinkEmailHtmlContent_Success() {
        // Given
        String username = "testuser";
        String magicLinkUrl = "https://example.com/verify?token=abc123";

        // When
        String result = emailTemplateService.generateMagicLinkEmailHtmlContent(username, magicLinkUrl);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Hello testuser"));
        assertTrue(result.contains("https://example.com/verify?token=abc123"));
        assertTrue(result.contains("Sign in to BlogApp"));
        assertTrue(result.contains("15 minutes"));
    }

    @Test
    void generateMagicLinkEmailTextContent_Success() {
        // Given
        String username = "testuser";
        String magicLinkUrl = "https://example.com/verify?token=abc123";

        // When
        String result = emailTemplateService.generateMagicLinkEmailTextContent(username, magicLinkUrl);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Hello testuser"));
        assertTrue(result.contains("https://example.com/verify?token=abc123"));
        assertTrue(result.contains("BlogApp"));
        assertTrue(result.contains("15 minutes"));
    }

    @Test
    void generateMagicLinkEmailHtmlContent_WithNullUsername() {
        // Given
        String username = null;
        String magicLinkUrl = "https://example.com/verify?token=abc123";

        // When
        String result = emailTemplateService.generateMagicLinkEmailHtmlContent(username, magicLinkUrl);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Hello there"));
        assertTrue(result.contains("https://example.com/verify?token=abc123"));
    }

    @Test
    void generateWelcomeEmailContent_Success() {
        // Given
        String username = "newuser";

        // When
        String result = emailTemplateService.generateWelcomeEmailContent(username);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Welcome to BlogApp, newuser"));
        assertTrue(result.contains("successfully created"));
    }

    @Test
    void generatePasswordResetEmailContent_Success() {
        // Given
        String username = "testuser";
        String resetLink = "https://example.com/reset?token=xyz789";

        // When
        String result = emailTemplateService.generatePasswordResetEmailContent(username, resetLink);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Hello testuser"));
        assertTrue(result.contains("https://example.com/reset?token=xyz789"));
        assertTrue(result.contains("reset your password"));
    }
} 