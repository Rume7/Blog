package com.codehacks.subscription.dto;

import com.codehacks.subscription.model.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {

    @Email(message = "Please provide a valid email address")
    @NotNull(message = "Email is required")
    private String email;

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    // Optional fields for future enhancement
    private String firstName;
    private String lastName;
    private String source; // Where the subscription came from (website, social media, etc.)
} 