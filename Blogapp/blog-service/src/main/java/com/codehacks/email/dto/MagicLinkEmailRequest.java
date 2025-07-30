package com.codehacks.email.dto;

/**
 * Request DTO for magic link email operations
 */
public record MagicLinkEmailRequest(
    String email,
    String username
) {
} 