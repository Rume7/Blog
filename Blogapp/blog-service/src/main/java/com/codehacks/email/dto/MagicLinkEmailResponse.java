package com.codehacks.email.dto;

/**
 * Response DTO for magic link email operations
 */
public record MagicLinkEmailResponse(
    String email,
    String username
) {
} 