package com.codehacks.image.dto;

import com.codehacks.image.model.ImageType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for image upload requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadRequest {

    @NotNull(message = "Image type is required")
    private ImageType imageType;

    private String altText; // Optional alternative text for accessibility

    private String description; // Optional description
} 