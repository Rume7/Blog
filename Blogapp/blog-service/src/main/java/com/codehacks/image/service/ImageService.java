package com.codehacks.image.service;

import com.codehacks.image.dto.ImageResponse;
import com.codehacks.image.model.ImageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for image management operations.
 */
public interface ImageService {

    /**
     * Upload an image file
     */
    ImageResponse uploadImage(MultipartFile file, ImageType imageType, String altText, String description, Long uploaderId);

    /**
     * Upload and set profile picture for a user (replaces existing one)
     */
    ImageResponse uploadProfilePicture(MultipartFile file, String altText, String description, Long userId);

    /**
     * Get an image by ID
     */
    Optional<ImageResponse> getImageById(Long id);

    /**
     * Get all images by type
     */
    List<ImageResponse> getImagesByType(ImageType imageType);

    /**
     * Get all images with pagination
     */
    Page<ImageResponse> getAllImages(Pageable pageable);

    /**
     * Get image file as byte array
     */
    byte[] getImageFile(Long id);

    /**
     * Get user's profile picture
     */
    Optional<ImageResponse> getUserProfilePicture(Long userId);

    /**
     * Validate image file
     */
    boolean isValidImageFile(MultipartFile file);

    /**
     * Get image statistics
     */
    long countImagesByUploaderId(Long uploaderId);

    /**
     * Get image statistics by type
     */
    long countImagesByType(ImageType imageType);

    /**
     * Get total storage used by uploader
     */
    long getTotalStorageUsedByUploaderId(Long uploaderId);
} 