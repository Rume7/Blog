package com.codehacks.image.dto;

import com.codehacks.image.model.Image;
import com.codehacks.image.model.ImageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for image responses in API endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {

    private Long id;
    private String fileName;
    private String storedFileName;
    private String filePath;
    private String contentType;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private ImageType imageType;
    private Long uploaderId;
    private String uploaderName;
    private String altText;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ImageResponse fromEntity(Image image) {
        return ImageResponse.builder()
                .id(image.getId())
                .fileName(image.getFileName())
                .storedFileName(image.getStoredFileName())
                .filePath(image.getFilePath())
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .width(image.getWidth())
                .height(image.getHeight())
                .imageType(image.getImageType())
                .uploaderId(image.getUploader().getId())
                .uploaderName(image.getUploader().getFirstName() + " " + image.getUploader().getLastName())
                .altText(image.getAltText())
                .description(image.getDescription())
                .isActive(image.getIsActive())
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .build();
    }
} 