package com.codehacks.image.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageStatsResponse {
    private Long uploaderId;
    private Long totalImages;
    private Long totalStorageBytes;
} 