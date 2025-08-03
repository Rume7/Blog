package com.codehacks.email.service;

import com.codehacks.email.dto.MagicLinkEmailRequest;
import com.codehacks.email.dto.MagicLinkEmailResponse;
import com.codehacks.email.exception.EmailServiceException;
import com.codehacks.email.model.MagicLinkToken;
import com.codehacks.email.repository.MagicLinkTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MagicLinkTokenRepository magicLinkTokenRepository;

    @Mock
    private EmailTemplateService emailTemplateService;

    @InjectMocks
    private EmailService emailService;

    private MagicLinkEmailRequest testRequest;
    private MagicLinkToken testToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "magicLinkBaseUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(emailService, "magicLinkExpirationMinutes", 15);
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");

        testRequest = new MagicLinkEmailRequest("test@example.com", "testuser");

        testToken = MagicLinkToken.builder()
                .id(1L)
                .email("test@example.com")
                .token("test-token-123")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
    }

    @Test
    void sendMagicLinkEmail_Success() {
        // Given
        when(emailTemplateService.generateMagicLinkEmailHtmlContent(anyString(), anyString()))
                .thenReturn("Test email content");
        when(magicLinkTokenRepository.save(any(MagicLinkToken.class)))
                .thenReturn(testToken);

        // When
        MagicLinkEmailResponse response = emailService.sendMagicLinkEmail(testRequest);

        // Then
        assertNotNull(response);
        assertEquals("test@example.com", response.email());
        assertEquals("Magic link sent successfully", response.message());
        assertNotNull(response.expiresAt());

        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(magicLinkTokenRepository).save(any(MagicLinkToken.class));
        verify(emailTemplateService).generateMagicLinkEmailHtmlContent(anyString(), anyString());
    }

    @Test
    void sendMagicLinkEmail_ThrowsException() {
        // Given
        when(magicLinkTokenRepository.save(any(MagicLinkToken.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(EmailServiceException.class, () -> {
            emailService.sendMagicLinkEmail(testRequest);
        });
    }

    @Test
    void validateMagicLinkToken_ValidToken() {
        // Given
        when(magicLinkTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(testToken));

        // When
        boolean isValid = emailService.validateMagicLinkToken("valid-token");

        // Then
        assertTrue(isValid);
        verify(magicLinkTokenRepository).save(any(MagicLinkToken.class));
    }

    @Test
    void validateMagicLinkToken_TokenNotFound() {
        // Given
        when(magicLinkTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        // When
        boolean isValid = emailService.validateMagicLinkToken("invalid-token");

        // Then
        assertFalse(isValid);
        verify(magicLinkTokenRepository, never()).save(any(MagicLinkToken.class));
    }

    @Test
    void validateMagicLinkToken_TokenAlreadyUsed() {
        // Given
        testToken.setUsed(true);
        when(magicLinkTokenRepository.findByToken("used-token"))
                .thenReturn(Optional.of(testToken));

        // When
        boolean isValid = emailService.validateMagicLinkToken("used-token");

        // Then
        assertFalse(isValid);
        verify(magicLinkTokenRepository, never()).save(any(MagicLinkToken.class));
    }

    @Test
    void validateMagicLinkToken_TokenExpired() {
        // Given
        testToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(magicLinkTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(testToken));

        // When
        boolean isValid = emailService.validateMagicLinkToken("expired-token");

        // Then
        assertFalse(isValid);
        verify(magicLinkTokenRepository, never()).save(any(MagicLinkToken.class));
    }

    @Test
    void getEmailFromToken_Success() {
        // Given
        when(magicLinkTokenRepository.findByToken("test-token"))
                .thenReturn(Optional.of(testToken));

        // When
        String email = emailService.getEmailFromToken("test-token");

        // Then
        assertEquals("test@example.com", email);
    }

    @Test
    void getEmailFromToken_TokenNotFound() {
        // Given
        when(magicLinkTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        // When
        String email = emailService.getEmailFromToken("invalid-token");

        // Then
        assertNull(email);
    }

    @Test
    void cleanupExpiredTokens_Success() {
        // When
        emailService.cleanupExpiredTokens();

        // Then
        verify(magicLinkTokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredTokens_ThrowsException() {
        // Given
        doThrow(new RuntimeException("Cleanup error"))
                .when(magicLinkTokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));

        // When & Then
        assertDoesNotThrow(() -> emailService.cleanupExpiredTokens());
    }
} 