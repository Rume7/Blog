package com.codehacks.email.scheduler;

import com.codehacks.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(EmailCleanupScheduler.class);

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