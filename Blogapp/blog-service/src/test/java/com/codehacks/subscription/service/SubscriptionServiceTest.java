package com.codehacks.subscription.service;

import com.codehacks.subscription.dto.SubscriptionRequest;
import com.codehacks.subscription.dto.SubscriptionResponse;
import com.codehacks.subscription.dto.SubscriptionStatistics;
import com.codehacks.subscription.model.NotificationLog;
import com.codehacks.subscription.model.NotificationType;
import com.codehacks.subscription.model.Subscription;
import com.codehacks.subscription.model.SubscriptionStatus;
import com.codehacks.subscription.repository.NotificationLogRepository;
import com.codehacks.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Subscription testSubscription;
    private SubscriptionRequest testRequest;

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
                .createdAt(LocalDateTime.now())
                .build();

        testRequest = SubscriptionRequest.builder()
                .email("test@example.com")
                .notificationType(NotificationType.INSTANT)
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void shouldCreateSubscriptionSuccessfully() {
        // Given
        when(subscriptionRepository.existsByEmail(anyString())).thenReturn(false);
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailNotificationService).sendWelcomeEmail(any(Subscription.class));

        // When
        SubscriptionResponse response = subscriptionService.createSubscription(testRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(response.getNotificationType()).isEqualTo(NotificationType.INSTANT);
        assertThat(response.isEmailVerified()).isTrue();

        verify(subscriptionRepository).existsByEmail("test@example.com");
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(emailNotificationService).sendWelcomeEmail(any(Subscription.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadySubscribed() {
        // Given
        when(subscriptionRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> subscriptionService.createSubscription(testRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already subscribed: test@example.com");

        verify(subscriptionRepository).existsByEmail("test@example.com");
        verify(subscriptionRepository, never()).save(any(Subscription.class));
        verify(emailNotificationService, never()).sendWelcomeEmail(any(Subscription.class));
    }

    @Test
    void shouldThrowExceptionWhenTokenNotFound() {
        // Given
        when(subscriptionRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.verifySubscription("invalid-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid verification token");

        verify(subscriptionRepository).findByToken("invalid-token");
        verify(subscriptionRepository, never()).save(any(Subscription.class));
        verify(emailNotificationService, never()).sendWelcomeEmail(any(Subscription.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyVerified() {
        // Given
        testSubscription.setEmailVerified(true);
        when(subscriptionRepository.findByToken("test-token-123")).thenReturn(Optional.of(testSubscription));

        // When & Then
        assertThatThrownBy(() -> subscriptionService.verifySubscription("test-token-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already verified");

        verify(subscriptionRepository).findByToken("test-token-123");
        verify(subscriptionRepository, never()).save(any(Subscription.class));
        verify(emailNotificationService, never()).sendWelcomeEmail(any(Subscription.class));
    }

    @Test
    void shouldUnsubscribeSuccessfully() {
        // Given
        when(subscriptionRepository.findByToken("test-token-123")).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);
        doNothing().when(emailNotificationService).sendUnsubscribeConfirmationEmail(any(Subscription.class));

        // When
        subscriptionService.unsubscribe("test-token-123");

        // Then
        verify(subscriptionRepository).findByToken("test-token-123");
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(emailNotificationService).sendUnsubscribeConfirmationEmail(any(Subscription.class));
    }

    @Test
    void shouldThrowExceptionWhenUnsubscribeTokenNotFound() {
        // Given
        when(subscriptionRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.unsubscribe("invalid-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid unsubscribe token");

        verify(subscriptionRepository).findByToken("invalid-token");
        verify(subscriptionRepository, never()).save(any(Subscription.class));
        verify(emailNotificationService, never()).sendUnsubscribeConfirmationEmail(any(Subscription.class));
    }

    @Test
    void shouldUpdatePreferencesSuccessfully() {
        // Given
        when(subscriptionRepository.findByToken("test-token-123")).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // When
        SubscriptionResponse response = subscriptionService.updatePreferences("test-token-123", NotificationType.DAILY);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getNotificationType()).isEqualTo(NotificationType.DAILY);

        verify(subscriptionRepository).findByToken("test-token-123");
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatePreferencesTokenNotFound() {
        // Given
        when(subscriptionRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.updatePreferences("invalid-token", NotificationType.DAILY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid token");

        verify(subscriptionRepository).findByToken("invalid-token");
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void shouldGetSubscriptionByEmail() {
        // Given
        when(subscriptionRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testSubscription));

        // When
        Optional<SubscriptionResponse> response = subscriptionService.getSubscriptionByEmail("test@example.com");

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getEmail()).isEqualTo("test@example.com");

        verify(subscriptionRepository).findByEmail("test@example.com");
    }

    @Test
    void shouldGetSubscriptionByToken() {
        // Given
        when(subscriptionRepository.findByToken("test-token-123")).thenReturn(Optional.of(testSubscription));

        // When
        Optional<SubscriptionResponse> response = subscriptionService.getSubscriptionByToken("test-token-123");

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getEmail()).isEqualTo("test@example.com");

        verify(subscriptionRepository).findByToken("test-token-123");
    }

    @Test
    void shouldGetActiveSubscriptions() {
        // Given
        List<Subscription> subscriptions = Arrays.asList(testSubscription);
        when(subscriptionRepository.findByStatusAndActiveTrue(SubscriptionStatus.ACTIVE)).thenReturn(subscriptions);

        // When
        List<SubscriptionResponse> responses = subscriptionService.getActiveSubscriptions();

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getEmail()).isEqualTo("test@example.com");

        verify(subscriptionRepository).findByStatusAndActiveTrue(SubscriptionStatus.ACTIVE);
    }

    @Test
    void shouldGetSubscriptionsForInstantNotification() {
        // Given
        List<Subscription> subscriptions = Arrays.asList(testSubscription);
        when(subscriptionRepository.findByStatusAndNotificationTypeAndActiveTrue(
                SubscriptionStatus.ACTIVE, NotificationType.INSTANT)).thenReturn(subscriptions);

        // When
        List<Subscription> result = subscriptionService.getSubscriptionsForInstantNotification();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");

        verify(subscriptionRepository).findByStatusAndNotificationTypeAndActiveTrue(
                SubscriptionStatus.ACTIVE, NotificationType.INSTANT);
    }

    @Test
    void shouldLogNotificationSent() {
        // Given
        when(notificationLogRepository.save(any(NotificationLog.class))).thenReturn(new NotificationLog());
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // When
        subscriptionService.logNotificationSent(testSubscription, "Test Subject", "Test Content", 1L);

        // Then
        verify(notificationLogRepository).save(any(NotificationLog.class));
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void shouldLogNotificationFailure() {
        // Given
        when(notificationLogRepository.save(any(NotificationLog.class))).thenReturn(new NotificationLog());

        // When
        subscriptionService.logNotificationFailure(testSubscription, "Test Subject", "Test Content", "Error message", 1L);

        // Then
        verify(notificationLogRepository).save(any(NotificationLog.class));
    }

    @Test
    void shouldGetStatistics() {
        // Given
        when(subscriptionRepository.countByStatusAndActiveTrue(SubscriptionStatus.ACTIVE)).thenReturn(15L);
        when(subscriptionRepository.countByStatusAndActiveTrue(SubscriptionStatus.PENDING)).thenReturn(0L);
        when(subscriptionRepository.countByStatusAndActiveTrue(SubscriptionStatus.INACTIVE)).thenReturn(2L);

        // When
        SubscriptionStatistics statistics = subscriptionService.getStatistics();

        // Then
        assertThat(statistics).isNotNull();
        assertThat(statistics.getTotalActive()).isEqualTo(15L);
        assertThat(statistics.getTotalPending()).isEqualTo(0L);
        assertThat(statistics.getTotalInactive()).isEqualTo(2L);
        assertThat(statistics.getTotalSubscriptions()).isEqualTo(17L);

        verify(subscriptionRepository).countByStatusAndActiveTrue(SubscriptionStatus.ACTIVE);
        verify(subscriptionRepository).countByStatusAndActiveTrue(SubscriptionStatus.PENDING);
        verify(subscriptionRepository).countByStatusAndActiveTrue(SubscriptionStatus.INACTIVE);
    }
} 