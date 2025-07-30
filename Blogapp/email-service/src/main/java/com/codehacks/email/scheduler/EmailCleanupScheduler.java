package com.codehacks.email.scheduler;

import com.codehacks.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailCleanupScheduler {

    private final EmailService emailService;

    /**
     * Scheduled task that runs every hour to clean up expired magic link tokens
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredTokens() {
        log.debug("Starting scheduled cleanup of expired magic link tokens");
        emailService.cleanupExpiredTokens();
    }
} 