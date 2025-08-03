package com.codehacks.subscription.service;

import com.codehacks.email.client.EmailServiceClient;
import com.codehacks.email.dto.MagicLinkEmailRequest;
import com.codehacks.subscription.model.NotificationType;
import com.codehacks.subscription.model.Subscription;
import com.codehacks.subscription.model.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceImplTest {

    @Mock
    private EmailServiceClient emailServiceClient;

    @InjectMocks
    private EmailNotificationServiceImpl emailNotificationService;

    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        testSubscription = Subscription.builder()
                .id(1L)
                .email("test@example.com")
                .token("test-token-123")
                .status(SubscriptionStatus.PENDING)
                .notificationType(NotificationType.INSTANT)
                .emailVerified(false)
                .active(true)
                .build();

        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(emailNotificationService, "baseUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(emailNotificationService, "blogName", "Test Blog");
    }

    @Test
    void shouldSendVerificationEmailSuccessfully() {
        // Given
        doNothing().when(emailServiceClient).sendSubscriptionVerificationEmail(anyString(), anyString());

        // When
        emailNotificationService.sendVerificationEmail(testSubscription);

        // Then
        verify(emailServiceClient).sendSubscriptionVerificationEmail(
            testSubscription.getEmail(),
            "http://localhost:3000/verify-subscription?token=" + testSubscription.getToken()
        );
    }

    @Test
    void shouldSendWelcomeEmailSuccessfully() {
        // Given
        doNothing().when(emailServiceClient).sendSubscriptionWelcomeEmail(anyString(), anyString());

        // When
        emailNotificationService.sendWelcomeEmail(testSubscription);

        // Then
        verify(emailServiceClient).sendSubscriptionWelcomeEmail(
            testSubscription.getEmail(),
            "http://localhost:3000"
        );
    }

    @Test
    void shouldSendUnsubscribeConfirmationEmailSuccessfully() {
        // Given
        doNothing().when(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));

        // When
        emailNotificationService.sendUnsubscribeConfirmationEmail(testSubscription);

        // Then
        verify(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));
    }

    @Test
    void shouldSendNewPostNotificationSuccessfully() {
        // Given
        doNothing().when(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));

        // When
        emailNotificationService.sendNewPostNotification(testSubscription, "Test Post", "http://localhost:3000/posts/1");

        // Then
        verify(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));
    }

    @Test
    void shouldSendDailyDigestSuccessfully() {
        // Given
        doNothing().when(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));

        // When
        emailNotificationService.sendDailyDigest(testSubscription, "<p>Daily digest content</p>");

        // Then
        verify(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));
    }

    @Test
    void shouldSendWeeklyDigestSuccessfully() {
        // Given
        doNothing().when(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));

        // When
        emailNotificationService.sendWeeklyDigest(testSubscription, "<p>Weekly digest content</p>");

        // Then
        verify(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));
    }

    @Test
    void shouldHandleEmailServiceException() {
        // Given
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailServiceClient).sendSubscriptionVerificationEmail(anyString(), anyString());

        // When & Then
        assertThatThrownBy(() -> emailNotificationService.sendVerificationEmail(testSubscription))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send verification email");

        verify(emailServiceClient).sendSubscriptionVerificationEmail(anyString(), anyString());
    }

    @Test
    void shouldHandleWelcomeEmailException() {
        // Given
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailServiceClient).sendSubscriptionWelcomeEmail(anyString(), anyString());

        // When & Then
        assertThatThrownBy(() -> emailNotificationService.sendWelcomeEmail(testSubscription))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send welcome email");

        verify(emailServiceClient).sendSubscriptionWelcomeEmail(anyString(), anyString());
    }

    @Test
    void shouldHandleUnsubscribeEmailException() {
        // Given
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));

        // When & Then
        assertThatThrownBy(() -> emailNotificationService.sendUnsubscribeConfirmationEmail(testSubscription))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send unsubscribe confirmation email");

        verify(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));
    }

    @Test
    void shouldHandleNewPostNotificationException() {
        // Given
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));

        // When & Then
        assertThatThrownBy(() -> emailNotificationService.sendNewPostNotification(testSubscription, "Test Post", "http://localhost:3000/posts/1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send new post notification");

        verify(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));
    }

    @Test
    void shouldHandleDailyDigestException() {
        // Given
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));

        // When & Then
        assertThatThrownBy(() -> emailNotificationService.sendDailyDigest(testSubscription, "<p>Daily digest content</p>"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send daily digest");

        verify(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));
    }

    @Test
    void shouldHandleWeeklyDigestException() {
        // Given
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));

        // When & Then
        assertThatThrownBy(() -> emailNotificationService.sendWeeklyDigest(testSubscription, "<p>Weekly digest content</p>"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send weekly digest");

        verify(emailServiceClient).sendMagicLinkEmail(any(MagicLinkEmailRequest.class));
    }
} 