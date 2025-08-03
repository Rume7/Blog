package com.codehacks.email.client;

import com.codehacks.email.dto.MagicLinkEmailRequest;
import com.codehacks.email.dto.MagicLinkEmailResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class EmailServiceClient {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${app.email-service.base-url:http://email-service:8080}")
    private String emailServiceBaseUrl;

    /**
     * Send magic link email via HTTP call to email service
     */
    public void sendMagicLinkEmail(MagicLinkEmailRequest request) {
        String url = emailServiceBaseUrl + "/api/v1/email/magic-link";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<MagicLinkEmailRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            restTemplate.postForEntity(url, entity, Void.class);
            log.info("Magic link email sent successfully to: {}", request.email());
        } catch (Exception e) {
            log.error("Failed to send magic link email to: {}", request.email(), e);
            throw new RuntimeException("Failed to send magic link email", e);
        }
    }

    /**
     * Send subscription welcome email via HTTP call to email service
     */
    public void sendSubscriptionWelcomeEmail(String email, String blogUrl) {
        String url = emailServiceBaseUrl + "/api/v1/email/subscription/welcome";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        String requestBody = String.format("email=%s&blogUrl=%s", 
                java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(blogUrl != null ? blogUrl : "", java.nio.charset.StandardCharsets.UTF_8));
        
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            restTemplate.postForEntity(url, entity, String.class);
            log.info("Subscription welcome email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send subscription welcome email to: {}", email, e);
            throw new RuntimeException("Failed to send subscription welcome email", e);
        }
    }

    /**
     * Validate magic link token via HTTP call to email service
     */
    public boolean validateMagicLinkToken(String token) {
        String url = emailServiceBaseUrl + "/api/v1/email/validate-token?token=" + token;
        
        try {
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            log.error("Failed to validate magic link token", e);
            return false;
        }
    }

    /**
     * Get email from token via HTTP call to email service
     */
    public String getEmailFromToken(String token) {
        String url = emailServiceBaseUrl + "/api/v1/email/email-from-token?token=" + token;
        
        try {
            String email = restTemplate.getForObject(url, String.class);
            return email;
        } catch (Exception e) {
            log.error("Failed to get email from token", e);
            throw new RuntimeException("Failed to get email from token", e);
        }
    }
} 