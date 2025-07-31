package com.codehacks.subscription.repository;

import com.codehacks.subscription.model.NotificationType;
import com.codehacks.subscription.model.Subscription;
import com.codehacks.subscription.model.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Testcontainers
class SubscriptionRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.8-alpine")
            .withDatabaseName("blog_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private Subscription testSubscription1;
    private Subscription testSubscription2;
    private Subscription testSubscription3;

    @BeforeEach
    void setUp() {
        subscriptionRepository.deleteAll();

        testSubscription1 = Subscription.builder()
                .email("test1@example.com")
                .token("token-1")
                .status(SubscriptionStatus.ACTIVE)
                .notificationType(NotificationType.INSTANT)
                .emailVerified(true)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        testSubscription2 = Subscription.builder()
                .email("test2@example.com")
                .token("token-2")
                .status(SubscriptionStatus.ACTIVE)
                .notificationType(NotificationType.DAILY)
                .emailVerified(true)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        testSubscription3 = Subscription.builder()
                .email("test3@example.com")
                .token("token-3")
                .status(SubscriptionStatus.PENDING)
                .notificationType(NotificationType.WEEKLY)
                .emailVerified(false)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldSaveAndFindSubscription() {
        // Given
        Subscription saved = subscriptionRepository.save(testSubscription1);

        // When
        Optional<Subscription> found = subscriptionRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test1@example.com");
        assertThat(found.get().getToken()).isEqualTo("token-1");
        assertThat(found.get().getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void shouldFindByEmail() {
        // Given
        subscriptionRepository.save(testSubscription1);

        // When
        Optional<Subscription> found = subscriptionRepository.findByEmail("test1@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test1@example.com");
    }

    @Test
    void shouldFindByToken() {
        // Given
        subscriptionRepository.save(testSubscription1);

        // When
        Optional<Subscription> found = subscriptionRepository.findByToken("token-1");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isEqualTo("token-1");
    }

    @Test
    void shouldFindActiveSubscriptionsByNotificationType() {
        // Given
        subscriptionRepository.save(testSubscription1); // INSTANT
        subscriptionRepository.save(testSubscription2); // DAILY
        subscriptionRepository.save(testSubscription3); // WEEKLY, PENDING

        // When
        List<Subscription> instantSubscriptions = subscriptionRepository
                .findByStatusAndNotificationTypeAndActiveTrue(SubscriptionStatus.ACTIVE, NotificationType.INSTANT);

        List<Subscription> dailySubscriptions = subscriptionRepository
                .findByStatusAndNotificationTypeAndActiveTrue(SubscriptionStatus.ACTIVE, NotificationType.DAILY);

        // Then
        assertThat(instantSubscriptions).hasSize(1);
        assertThat(instantSubscriptions.get(0).getEmail()).isEqualTo("test1@example.com");

        assertThat(dailySubscriptions).hasSize(1);
        assertThat(dailySubscriptions.get(0).getEmail()).isEqualTo("test2@example.com");
    }

    @Test
    void shouldFindActiveSubscriptionsForNotification() {
        // Given
        testSubscription1.setLastNotificationSent(LocalDateTime.now().minusDays(2));
        testSubscription2.setLastNotificationSent(LocalDateTime.now().minusDays(2));
        testSubscription3.setLastNotificationSent(LocalDateTime.now().minusDays(2));

        subscriptionRepository.save(testSubscription1);
        subscriptionRepository.save(testSubscription2);
        subscriptionRepository.save(testSubscription3);

        LocalDateTime since = LocalDateTime.now().minusDays(1);

        // When
        List<Subscription> instantSubscriptions = subscriptionRepository
                .findActiveSubscriptionsForNotification(SubscriptionStatus.ACTIVE, NotificationType.INSTANT, since);

        List<Subscription> dailySubscriptions = subscriptionRepository
                .findActiveSubscriptionsForNotification(SubscriptionStatus.ACTIVE, NotificationType.DAILY, since);

        // Then
        assertThat(instantSubscriptions).hasSize(1);
        assertThat(instantSubscriptions.get(0).getEmail()).isEqualTo("test1@example.com");

        assertThat(dailySubscriptions).hasSize(1);
        assertThat(dailySubscriptions.get(0).getEmail()).isEqualTo("test2@example.com");
    }

    @Test
    void shouldCheckIfEmailExists() {
        // Given
        subscriptionRepository.save(testSubscription1);

        // When & Then
        assertThat(subscriptionRepository.existsByEmail("test1@example.com")).isTrue();
        assertThat(subscriptionRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    void shouldFindAllActiveSubscriptions() {
        // Given
        subscriptionRepository.save(testSubscription1); // ACTIVE
        subscriptionRepository.save(testSubscription2); // ACTIVE
        subscriptionRepository.save(testSubscription3); // PENDING

        // When
        List<Subscription> activeSubscriptions = subscriptionRepository
                .findByStatusAndActiveTrue(SubscriptionStatus.ACTIVE);

        // Then
        assertThat(activeSubscriptions).hasSize(2);
        assertThat(activeSubscriptions).extracting("email")
                .containsExactlyInAnyOrder("test1@example.com", "test2@example.com");
    }

    @Test
    void shouldCountActiveSubscriptions() {
        // Given
        subscriptionRepository.save(testSubscription1); // ACTIVE
        subscriptionRepository.save(testSubscription2); // ACTIVE
        subscriptionRepository.save(testSubscription3); // PENDING

        // When
        long activeCount = subscriptionRepository.countByStatusAndActiveTrue(SubscriptionStatus.ACTIVE);
        long pendingCount = subscriptionRepository.countByStatusAndActiveTrue(SubscriptionStatus.PENDING);

        // Then
        assertThat(activeCount).isEqualTo(2);
        assertThat(pendingCount).isEqualTo(1);
    }

    @Test
    void shouldNotFindSubscriptionsForNotificationWhenRecentlySent() {
        // Given
        testSubscription1.setLastNotificationSent(LocalDateTime.now().minusHours(1));
        testSubscription2.setLastNotificationSent(LocalDateTime.now().minusHours(1));

        subscriptionRepository.save(testSubscription1);
        subscriptionRepository.save(testSubscription2);

        LocalDateTime since = LocalDateTime.now().minusDays(1);

        // When
        List<Subscription> instantSubscriptions = subscriptionRepository
                .findActiveSubscriptionsForNotification(SubscriptionStatus.ACTIVE, NotificationType.INSTANT, since);

        List<Subscription> dailySubscriptions = subscriptionRepository
                .findActiveSubscriptionsForNotification(SubscriptionStatus.ACTIVE, NotificationType.DAILY, since);

        // Then
        assertThat(instantSubscriptions).isEmpty();
        assertThat(dailySubscriptions).isEmpty();
    }

    @Test
    void shouldFindSubscriptionsWithNullLastNotificationSent() {
        // Given
        testSubscription1.setLastNotificationSent(null);
        testSubscription2.setLastNotificationSent(null);

        subscriptionRepository.save(testSubscription1);
        subscriptionRepository.save(testSubscription2);

        LocalDateTime since = LocalDateTime.now().minusDays(1);

        // When
        List<Subscription> instantSubscriptions = subscriptionRepository
                .findActiveSubscriptionsForNotification(SubscriptionStatus.ACTIVE, NotificationType.INSTANT, since);

        List<Subscription> dailySubscriptions = subscriptionRepository
                .findActiveSubscriptionsForNotification(SubscriptionStatus.ACTIVE, NotificationType.DAILY, since);

        // Then
        assertThat(instantSubscriptions).hasSize(1);
        assertThat(dailySubscriptions).hasSize(1);
    }
} 