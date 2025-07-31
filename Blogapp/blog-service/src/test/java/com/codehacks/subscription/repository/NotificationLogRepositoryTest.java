package com.codehacks.subscription.repository;

import com.codehacks.subscription.model.NotificationLog;
import com.codehacks.subscription.model.NotificationStatus;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Testcontainers
class NotificationLogRepositoryTest {

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
    private NotificationLogRepository notificationLogRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private Subscription testSubscription;
    private NotificationLog testLog1;
    private NotificationLog testLog2;
    private NotificationLog testLog3;

    @BeforeEach
    void setUp() {
        notificationLogRepository.deleteAll();
        subscriptionRepository.deleteAll();

        // Create test subscription
        testSubscription = Subscription.builder()
                .email("test@example.com")
                .token("test-token")
                .status(SubscriptionStatus.ACTIVE)
                .notificationType(NotificationType.INSTANT)
                .emailVerified(true)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        testSubscription = subscriptionRepository.save(testSubscription);

        // Create test notification logs
        testLog1 = NotificationLog.builder()
                .subscription(testSubscription)
                .email("test@example.com")
                .notificationType(NotificationType.INSTANT)
                .subject("Test Subject 1")
                .content("Test Content 1")
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now().minusDays(1))
                .postId(1L)
                .build();

        testLog2 = NotificationLog.builder()
                .subscription(testSubscription)
                .email("test@example.com")
                .notificationType(NotificationType.DAILY)
                .subject("Test Subject 2")
                .content("Test Content 2")
                .status(NotificationStatus.FAILED)
                .errorMessage("Test error")
                .postId(2L)
                .build();

        testLog3 = NotificationLog.builder()
                .subscription(testSubscription)
                .email("test@example.com")
                .notificationType(NotificationType.WEEKLY)
                .subject("Test Subject 3")
                .content("Test Content 3")
                .status(NotificationStatus.PENDING)
                .postId(3L)
                .build();
    }

    @Test
    void shouldSaveAndFindNotificationLog() {
        // Given
        NotificationLog saved = notificationLogRepository.save(testLog1);

        // When
        NotificationLog found = notificationLogRepository.findById(saved.getId()).orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("test@example.com");
        assertThat(found.getSubject()).isEqualTo("Test Subject 1");
        assertThat(found.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(found.getPostId()).isEqualTo(1L);
    }

    @Test
    void shouldFindBySubscriptionId() {
        // Given
        notificationLogRepository.save(testLog1);
        notificationLogRepository.save(testLog2);

        // When
        List<NotificationLog> logs = notificationLogRepository
                .findBySubscriptionIdOrderByCreatedAtDesc(testSubscription.getId());

        // Then
        assertThat(logs).hasSize(2);
        assertThat(logs).extracting("subject")
                .containsExactlyInAnyOrder("Test Subject 1", "Test Subject 2");
    }

    @Test
    void shouldFindByStatus() {
        // Given
        notificationLogRepository.save(testLog1); // SENT
        notificationLogRepository.save(testLog2); // FAILED
        notificationLogRepository.save(testLog3); // PENDING

        // When
        List<NotificationLog> sentLogs = notificationLogRepository.findByStatus(NotificationStatus.SENT);
        List<NotificationLog> failedLogs = notificationLogRepository.findByStatus(NotificationStatus.FAILED);

        // Then
        assertThat(sentLogs).hasSize(1);
        assertThat(sentLogs.get(0).getSubject()).isEqualTo("Test Subject 1");

        assertThat(failedLogs).hasSize(1);
        assertThat(failedLogs.get(0).getSubject()).isEqualTo("Test Subject 2");
    }

    @Test
    void shouldFindByEmail() {
        // Given
        notificationLogRepository.save(testLog1);
        notificationLogRepository.save(testLog2);

        // When
        List<NotificationLog> logs = notificationLogRepository
                .findByEmailOrderByCreatedAtDesc("test@example.com");

        // Then
        assertThat(logs).hasSize(2);
        assertThat(logs).extracting("email")
                .allMatch(email -> email.equals("test@example.com"));
    }

    @Test
    void shouldFindByCreatedAtAfter() {
        // Given
        notificationLogRepository.save(testLog1);
        notificationLogRepository.save(testLog2);

        LocalDateTime since = LocalDateTime.now().minusDays(2);

        // When
        List<NotificationLog> logs = notificationLogRepository.findByCreatedAtAfter(since);

        // Then
        assertThat(logs).hasSize(2);
    }

    @Test
    void shouldFindByStatusIn() {
        // Given
        notificationLogRepository.save(testLog1); // SENT
        notificationLogRepository.save(testLog2); // FAILED
        notificationLogRepository.save(testLog3); // PENDING

        List<NotificationStatus> statuses = Arrays.asList(NotificationStatus.FAILED, NotificationStatus.PENDING);

        // When
        List<NotificationLog> logs = notificationLogRepository.findByStatusIn(statuses);

        // Then
        assertThat(logs).hasSize(2);
        assertThat(logs).extracting("status")
                .containsExactlyInAnyOrder(NotificationStatus.FAILED, NotificationStatus.PENDING);
    }

    @Test
    void shouldCountByStatus() {
        // Given
        notificationLogRepository.save(testLog1); // SENT
        notificationLogRepository.save(testLog2); // FAILED
        notificationLogRepository.save(testLog3); // PENDING

        // When
        long sentCount = notificationLogRepository.countByStatus(NotificationStatus.SENT);
        long failedCount = notificationLogRepository.countByStatus(NotificationStatus.FAILED);
        long pendingCount = notificationLogRepository.countByStatus(NotificationStatus.PENDING);

        // Then
        assertThat(sentCount).isEqualTo(1);
        assertThat(failedCount).isEqualTo(1);
        assertThat(pendingCount).isEqualTo(1);
    }

    @Test
    void shouldFindByPostId() {
        // Given
        notificationLogRepository.save(testLog1); // postId = 1
        notificationLogRepository.save(testLog2); // postId = 2
        notificationLogRepository.save(testLog3); // postId = 3

        // When
        List<NotificationLog> logs = notificationLogRepository.findByPostIdOrderByCreatedAtDesc(1L);

        // Then
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getPostId()).isEqualTo(1L);
        assertThat(logs.get(0).getSubject()).isEqualTo("Test Subject 1");
    }

    @Test
    void shouldFindNotificationsForRetry() {
        // Given
        testLog2.setCreatedAt(LocalDateTime.now().minusHours(1)); // FAILED, recent
        testLog3.setCreatedAt(LocalDateTime.now().minusHours(1)); // PENDING, recent

        notificationLogRepository.save(testLog1); // SENT
        notificationLogRepository.save(testLog2); // FAILED, recent
        notificationLogRepository.save(testLog3); // PENDING, recent

        LocalDateTime since = LocalDateTime.now().minusDays(1);

        // When
        List<NotificationLog> logs = notificationLogRepository.findNotificationsForRetry(since);

        // Then
        assertThat(logs).hasSize(2);
        assertThat(logs).extracting("status")
                .containsExactlyInAnyOrder(NotificationStatus.FAILED, NotificationStatus.PENDING);
    }

    @Test
    void shouldNotFindNotificationsForRetryWhenOld() {
        // Given - Clean up any existing logs first
        notificationLogRepository.deleteAll();
        
        // Create logs with current timestamps
        NotificationLog oldFailedLog = NotificationLog.builder()
                .subscription(testSubscription)
                .email("test@example.com")
                .notificationType(NotificationType.DAILY)
                .subject("Old Failed Subject")
                .content("Old Failed Content")
                .status(NotificationStatus.FAILED)
                .errorMessage("Old error")
                .postId(4L)
                .build();

        NotificationLog oldPendingLog = NotificationLog.builder()
                .subscription(testSubscription)
                .email("test@example.com")
                .notificationType(NotificationType.WEEKLY)
                .subject("Old Pending Subject")
                .content("Old Pending Content")
                .status(NotificationStatus.PENDING)
                .postId(5L)
                .build();

        // Save the logs
        notificationLogRepository.save(oldFailedLog);
        notificationLogRepository.save(oldPendingLog);

        // Use a time that's in the future (so no logs should be found)
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);

        // When
        List<NotificationLog> logs = notificationLogRepository.findNotificationsForRetry(futureTime);

        // Then - No logs should be found because they were created before the future time
        assertThat(logs).isEmpty();
    }

    @Test
    void shouldOrderByCreatedAtDesc() {
        // Given
        testLog1.setCreatedAt(LocalDateTime.now().minusDays(1));
        testLog2.setCreatedAt(LocalDateTime.now().minusDays(2));
        testLog3.setCreatedAt(LocalDateTime.now().minusDays(3));

        notificationLogRepository.save(testLog1);
        notificationLogRepository.save(testLog2);
        notificationLogRepository.save(testLog3);

        // When
        List<NotificationLog> logs = notificationLogRepository
                .findBySubscriptionIdOrderByCreatedAtDesc(testSubscription.getId());

        // Then
        assertThat(logs).hasSize(3);
        assertThat(logs.get(0).getCreatedAt()).isAfter(logs.get(1).getCreatedAt());
        assertThat(logs.get(1).getCreatedAt()).isAfter(logs.get(2).getCreatedAt());
    }
} 