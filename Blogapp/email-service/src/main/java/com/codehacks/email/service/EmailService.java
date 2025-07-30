package com.codehacks.email.service;

import com.codehacks.email.dto.MagicLinkEmailRequest;
import com.codehacks.email.dto.MagicLinkEmailResponse;
import com.codehacks.email.exception.EmailServiceException;
import com.codehacks.email.model.MagicLinkToken;
import com.codehacks.email.repository.MagicLinkTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final MagicLinkTokenRepository magicLinkTokenRepository;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.magic-link.base-url:http://localhost:3000}")
    private String magicLinkBaseUrl;

    @Value("${app.magic-link.expiration-minutes:15}")
    private int magicLinkExpirationMinutes;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Generates and sends a magic link email to the user
     */
    @Transactional
    public MagicLinkEmailResponse sendMagicLinkEmail(MagicLinkEmailRequest request) {
        try {
            log.info("Generating magic link for user: {}", request.email());

            String token = generateSecureToken();
            MagicLinkToken magicLinkToken = createMagicLinkToken(request.email(), token);
            magicLinkTokenRepository.save(magicLinkToken);
            String magicLinkUrl = generateMagicLinkUrl(token);
            sendMagicLinkEmail(request.email(), magicLinkUrl, request.username());
            
            log.info("Magic link email sent successfully to: {}", request.email());
            
            return new MagicLinkEmailResponse(
                    request.email(),
                    "Magic link sent successfully",
                    magicLinkToken.getExpiresAt()
            );
                    
        } catch (Exception e) {
            log.error("Failed to send magic link email to: {}", request.email(), e);
            throw new EmailServiceException("Failed to send magic link email", e);
        }
    }

    /**
     * Validates a magic link token and marks it as used if valid
     */
    @Transactional
    public boolean validateMagicLinkToken(String token) {
        try {
            log.debug("Validating magic link token: {}", maskToken(token));
            
            MagicLinkToken magicLinkToken = magicLinkTokenRepository.findByToken(token)
                    .orElse(null);
            
            if (magicLinkToken == null) {
                log.warn("Magic link token not found: {}", maskToken(token));
                return false;
            }
            
            if (magicLinkToken.isUsed()) {
                log.warn("Magic link token already used: {}", maskToken(token));
                return false;
            }
            
            if (magicLinkToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("Magic link token expired: {}", maskToken(token));
                return false;
            }
            
            // Mark token as used to prevent reuse
            magicLinkToken.setUsed(true);
            magicLinkToken.setUsedAt(LocalDateTime.now());
            magicLinkTokenRepository.save(magicLinkToken);
            
            log.info("Magic link token validated successfully for user: {}", magicLinkToken.getEmail());
            return true;
            
        } catch (Exception e) {
            log.error("Error validating magic link token: {}", maskToken(token), e);
            return false;
        }
    }

    /**
     * Retrieves the email address associated with a magic link token
     */
    public String getEmailFromToken(String token) {
        return magicLinkTokenRepository.findByToken(token)
                .map(MagicLinkToken::getEmail)
                .orElse(null);
    }

    /**
     * Cleans up expired magic link tokens from the database
     */
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            long deletedCount = magicLinkTokenRepository.deleteByExpiresAtBefore(now);
            log.info("Cleaned up {} expired magic link tokens", deletedCount);
        } catch (Exception e) {
            log.error("Error cleaning up expired tokens", e);
        }
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private MagicLinkToken createMagicLinkToken(String email, String token) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(magicLinkExpirationMinutes);
        
        return MagicLinkToken.builder()
                .email(email)
                .token(token)
                .createdAt(now)
                .expiresAt(expiresAt)
                .used(false)
                .build();
    }

    private String generateMagicLinkUrl(String token) {
        return magicLinkBaseUrl + "/auth/verify?token=" + token;
    }

    private void sendMagicLinkEmail(String toEmail, String magicLinkUrl, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Magic Link - BlogApp");
            
            String emailContent = emailTemplateService.generateMagicLinkEmailHtmlContent(username, magicLinkUrl);
            message.setText(emailContent);
            
            mailSender.send(message);
            log.debug("Magic link email sent to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send email to: {}", toEmail, e);
            throw new EmailServiceException("Failed to send email", e);
        }
    }

    /**
     * Masks a token for secure logging (shows only first 4 and last 4 characters)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
} 