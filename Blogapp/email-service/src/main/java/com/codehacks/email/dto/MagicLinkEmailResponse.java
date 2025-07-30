package com.codehacks.email.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for magic link email operations
 */
public record MagicLinkEmailResponse(String email, String message, LocalDateTime expiresAt) {}