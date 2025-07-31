package com.codehacks.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatistics {

    private long totalSubscriptions;
    private long totalActive;
    private long totalPending;
    private long totalInactive;
    private long totalSuspended;
} 