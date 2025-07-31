package com.codehacks.subscription.controller;

import com.codehacks.subscription.dto.SubscriptionStatistics;
import com.codehacks.subscription.service.SubscriptionService;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerSecurityTest {

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionController subscriptionController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(subscriptionController).build();
        objectMapper.findAndRegisterModules();
        
        // Clear security context
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldDenyAccessToStatisticsWithoutAuthentication() throws Exception {
        // When & Then - No authentication
        mockMvc.perform(get("/api/v1/subscriptions/statistics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessToStatisticsWithUserRole() throws Exception {
        // Given - User with USER role
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(UserRole.USER);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // When & Then - User role should be denied
        mockMvc.perform(get("/api/v1/subscriptions/statistics"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAccessToStatisticsWithAdminRole() throws Exception {
        // Given - User with ADMIN role
        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@example.com");
        admin.setRole(UserRole.ADMIN);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                admin,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Given - Mock service response
        SubscriptionStatistics statistics = SubscriptionStatistics.builder()
                .totalSubscriptions(10L)
                .totalActive(5L)
                .totalPending(3L)
                .totalInactive(2L)
                .build();
        when(subscriptionService.getStatistics()).thenReturn(statistics);

        // When & Then - Admin role should be allowed
        mockMvc.perform(get("/api/v1/subscriptions/statistics"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessToAllSubscriptionsWithoutAuthentication() throws Exception {
        // When & Then - No authentication
        mockMvc.perform(get("/api/v1/subscriptions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessToAllSubscriptionsWithUserRole() throws Exception {
        // Given - User with USER role
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(UserRole.USER);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // When & Then - User role should be denied
        mockMvc.perform(get("/api/v1/subscriptions"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAccessToAllSubscriptionsWithAdminRole() throws Exception {
        // Given - User with ADMIN role
        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@example.com");
        admin.setRole(UserRole.ADMIN);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                admin,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Given - Mock service response
        when(subscriptionService.getActiveSubscriptions()).thenReturn(Collections.emptyList());

        // When & Then - Admin role should be allowed
        mockMvc.perform(get("/api/v1/subscriptions"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessToSubscriptionByEmailWithoutAuthentication() throws Exception {
        // When & Then - No authentication
        mockMvc.perform(get("/api/v1/subscriptions/email/test@example.com"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessToSubscriptionByEmailWithUserRole() throws Exception {
        // Given - User with USER role
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(UserRole.USER);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // When & Then - User role should be denied
        mockMvc.perform(get("/api/v1/subscriptions/email/test@example.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAccessToSubscriptionByEmailWithAdminRole() throws Exception {
        // Given - User with ADMIN role
        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@example.com");
        admin.setRole(UserRole.ADMIN);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                admin,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Given - Mock service response
        when(subscriptionService.getSubscriptionByEmail("test@example.com")).thenReturn(java.util.Optional.empty());

        // When & Then - Admin role should be allowed
        mockMvc.perform(get("/api/v1/subscriptions/email/test@example.com"))
                .andExpect(status().isNotFound());
    }
} 