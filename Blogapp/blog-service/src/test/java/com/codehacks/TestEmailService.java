package com.codehacks;

/**
 * Test interface for EmailService to avoid compilation issues
 * This mirrors the methods we need to mock in tests
 */
public interface TestEmailService {
    
    /**
     * Sends a magic link email
     */
    void sendMagicLinkEmail(Object request);
    
    /**
     * Validates a magic link token
     */
    boolean validateMagicLinkToken(String token);
    
    /**
     * Gets email from token
     */
    String getEmailFromToken(String token);
} 