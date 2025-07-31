package com.codehacks.subscription.dto;

import com.codehacks.subscription.model.NotificationType;
import com.codehacks.subscription.model.Subscription;
import com.codehacks.subscription.model.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private Long id;
    private String email;
    private String token;
    private SubscriptionStatus status;
    private NotificationType notificationType;
    private boolean emailVerified;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;

    /**
     * Convert Subscription entity to SubscriptionResponse
     */
    public static SubscriptionResponse fromSubscription(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .email(subscription.getEmail())
                .token(subscription.getToken())
                .status(subscription.getStatus())
                .notificationType(subscription.getNotificationType())
                .emailVerified(subscription.isEmailVerified())
                .active(subscription.isActive())
                .createdAt(subscription.getCreatedAt())
                .verifiedAt(subscription.getVerifiedAt())
                .build();
    }
} 