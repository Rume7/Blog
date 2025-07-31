package com.codehacks.image.dto;

import com.codehacks.image.model.ImageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageTypeStatsResponse {
    private ImageType imageType;
    private Long totalImages;
} 