package com.codehacks.subscription;

import com.codehacks.subscription.dto.SubscriptionRequest;
import com.codehacks.subscription.dto.SubscriptionResponse;
import com.codehacks.subscription.dto.SubscriptionStatistics;
import com.codehacks.subscription.model.NotificationType;
import com.codehacks.subscription.model.Subscription;
import com.codehacks.subscription.model.SubscriptionStatus;
import com.codehacks.subscription.repository.NotificationLogRepository;
import com.codehacks.subscription.repository.SubscriptionRepository;
import com.codehacks.subscription.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
class SubscriptionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.8-alpine")
            .withDatabaseName("blog_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("app.email-service.base-url", () -> "http://localhost:8081");
        registry.add("app.magic-link.base-url", () -> "http://localhost:3000");
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @MockBean
    private com.codehacks.subscription.service.EmailNotificationService emailNotificationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Enable LocalDateTime support
        
        // Mock email service to avoid actual email sending
        doNothing().when(emailNotificationService).sendVerificationEmail(any());
        doNothing().when(emailNotificationService).sendWelcomeEmail(any());
        doNothing().when(emailNotificationService).sendUnsubscribeConfirmationEmail(any());
        
        // Clean up test data
        notificationLogRepository.deleteAll();
        subscriptionRepository.deleteAll();
    }

    @Test
    void shouldCreateAndVerifySubscriptionWorkflow() throws Exception {
        // Given
        SubscriptionRequest request = SubscriptionRequest.builder()
                .email("integration@example.com")
                .notificationType(NotificationType.INSTANT)
                .firstName("Integration")
                .lastName("Test")
                .build();

        // When - Create subscription
        String responseJson = mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.notificationType").value("INSTANT"))
                .andExpect(jsonPath("$.emailVerified").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        SubscriptionResponse response = objectMapper.readValue(responseJson, SubscriptionResponse.class);
        String token = response.getToken();

        // Then - Verify subscription exists in database and is already active
        assertThat(subscriptionRepository.findByEmail("integration@example.com")).isPresent();
        assertThat(subscriptionRepository.findByToken(token)).isPresent();
        assertThat(subscriptionRepository.findByEmail("integration@example.com"))
                .isPresent()
                .hasValueSatisfying(sub -> {
                    assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
                    assertThat(sub.isEmailVerified()).isTrue();
                });
    }

    @Test
    void shouldHandleDuplicateEmailSubscription() throws Exception {
        // Given
        SubscriptionRequest request = SubscriptionRequest.builder()
                .email("duplicate@example.com")
                .notificationType(NotificationType.INSTANT)
                .build();

        // When - Create first subscription
        mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // When - Try to create duplicate subscription
        mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is already subscribed: duplicate@example.com"));

        // Then - Verify only one subscription exists
        assertThat(subscriptionRepository.findByEmail("duplicate@example.com")).isPresent();
        assertThat(subscriptionRepository.countByStatusAndActiveTrue(SubscriptionStatus.ACTIVE)).isEqualTo(1);
    }

    @Test
    void shouldUpdateSubscriptionPreferences() throws Exception {
        // Given - Create and verify subscription
        SubscriptionRequest request = SubscriptionRequest.builder()
                .email("preferences@example.com")
                .notificationType(NotificationType.INSTANT)
                .build();

        String responseJson = mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        SubscriptionResponse response = objectMapper.readValue(responseJson, SubscriptionResponse.class);
        String token = response.getToken();

        // When - Update preferences (subscription is already active)
        mockMvc.perform(put("/api/v1/subscriptions/" + token + "/preferences")
                        .param("notificationType", "DAILY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationType").value("DAILY"));

        // Then - Verify preferences updated
        assertThat(subscriptionRepository.findByToken(token))
                .isPresent()
                .hasValueSatisfying(sub -> {
                    assertThat(sub.getNotificationType()).isEqualTo(NotificationType.DAILY);
                });
    }

    @Test
    void shouldUnsubscribeSuccessfully() throws Exception {
        // Given - Create and verify subscription
        SubscriptionRequest request = SubscriptionRequest.builder()
                .email("unsubscribe@example.com")
                .notificationType(NotificationType.INSTANT)
                .build();

        String responseJson = mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        SubscriptionResponse response = objectMapper.readValue(responseJson, SubscriptionResponse.class);
        String token = response.getToken();

        // When - Unsubscribe (subscription is already active)
        mockMvc.perform(delete("/api/v1/subscriptions/" + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully unsubscribed"));

        // Then - Verify subscription is inactive
        assertThat(subscriptionRepository.findByToken(token))
                .isPresent()
                .hasValueSatisfying(sub -> {
                    assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.INACTIVE);
                    assertThat(sub.isActive()).isFalse();
                    assertThat(sub.getNotificationType()).isEqualTo(NotificationType.NONE);
                });
    }

    @Test
    void shouldGetSubscriptionStatistics() throws Exception {
        // Given - Create multiple subscriptions
        createTestSubscription("stats1@example.com", NotificationType.INSTANT);
        createTestSubscription("stats2@example.com", NotificationType.DAILY);
        createTestSubscription("stats3@example.com", NotificationType.WEEKLY);

        // When - Get statistics through the service directly (bypassing security for testing)
        SubscriptionStatistics statistics = subscriptionService.getStatistics();

        // Then
        assertThat(statistics.getTotalSubscriptions()).isEqualTo(3);
        assertThat(statistics.getTotalPending()).isEqualTo(0);
        assertThat(statistics.getTotalActive()).isEqualTo(3);
        assertThat(statistics.getTotalInactive()).isEqualTo(0);
    }

    @Test
    void shouldDenyAccessToStatisticsWithoutAdminRole() throws Exception {
        // When - Try to access statistics endpoint without authentication
        mockMvc.perform(get("/api/v1/subscriptions/statistics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandleInvalidTokenOperations() throws Exception {
        // When & Then - Try to verify with invalid token
        mockMvc.perform(get("/api/v1/subscriptions/verify/invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid verification token"));

        // When & Then - Try to unsubscribe with invalid token
        mockMvc.perform(delete("/api/v1/subscriptions/invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid unsubscribe token"));

        // When & Then - Try to update preferences with invalid token
        mockMvc.perform(put("/api/v1/subscriptions/invalid-token/preferences")
                        .param("notificationType", "DAILY"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid token"));
    }

    @Test
    void shouldLogNotificationSent() {
        // Given
        SubscriptionRequest request = SubscriptionRequest.builder()
                .email("logging@example.com")
                .notificationType(NotificationType.INSTANT)
                .build();

        SubscriptionResponse response = subscriptionService.createSubscription(request);
        String token = response.getToken();

        Subscription subscription = subscriptionRepository.findByToken(token).orElseThrow();

        // When
        subscriptionService.logNotificationSent(subscription, "Test Subject", "Test Content", 1L);

        // Then
        assertThat(notificationLogRepository.findBySubscriptionIdOrderByCreatedAtDesc(subscription.getId()))
                .hasSize(1)
                .first()
                .satisfies(log -> {
                    assertThat(log.getEmail()).isEqualTo("logging@example.com");
                    assertThat(log.getSubject()).isEqualTo("Test Subject");
                    assertThat(log.getPostId()).isEqualTo(1L);
                });
    }

    private void createTestSubscription(String email, NotificationType notificationType) {
        SubscriptionRequest request = SubscriptionRequest.builder()
                .email(email)
                .notificationType(notificationType)
                .build();
        subscriptionService.createSubscription(request);
    }
} 