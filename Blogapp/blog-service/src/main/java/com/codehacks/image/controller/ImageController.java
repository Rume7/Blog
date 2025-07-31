package com.codehacks.image.controller;

import com.codehacks.image.dto.ImageResponse;
import com.codehacks.image.dto.ImageStatsResponse;
import com.codehacks.image.dto.ImageTypeStatsResponse;
import com.codehacks.image.model.ImageType;
import com.codehacks.image.service.ImageService;
import com.codehacks.user.model.User;
import com.codehacks.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for managing image uploads and operations.
 */
@RestController
@RequestMapping(Constants.IMAGES_PATH)
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageService imageService;

    /**
     * Upload a new image
     * Requires authenticated user.
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("imageType") ImageType imageType,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "description", required = false) String description,
            @RequestAttribute("currentUser") User currentUser) {
        log.info("Uploading image of type: {} by user: {}", imageType, currentUser.getId());
        ImageResponse response = imageService.uploadImage(file, imageType, altText, description, currentUser.getId());
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Upload profile picture (replaces existing)
     * Requires authenticated user.
     */
    @PostMapping("/profile-picture")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<ImageResponse> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "description", required = false) String description,
            @RequestAttribute("currentUser") User currentUser) {
        log.info("Uploading profile picture for user: {}", currentUser.getId());
        ImageResponse response = imageService.uploadProfilePicture(file, altText, description, currentUser.getId());
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Get image by ID (public access for featured images only)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ImageResponse> getImageById(@PathVariable Long id) {
        log.info("Fetching image by ID: {}", id);
        return imageService.getImageById(id)
                .map(image -> {
                    // Only allow public access to featured images
                    if (image.getImageType() != ImageType.FEATURED_IMAGE) {
                        throw new SecurityException("Access denied. Only featured images are publicly accessible.");
                    }
                    return ResponseEntity.ok(image);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get image file by ID (public access for featured images only)
     */
    @GetMapping("/{id}/file")
    public ResponseEntity<ByteArrayResource> getImageFile(@PathVariable Long id) {
        log.info("Fetching image file by ID: {}", id);
        return imageService.getImageById(id)
                .map(image -> {
                    // Only allow public access to featured images
                    if (image.getImageType() != ImageType.FEATURED_IMAGE) {
                        throw new SecurityException("Access denied. Only featured images are publicly accessible.");
                    }
                    
                    byte[] imageData = imageService.getImageFile(id);
                    ByteArrayResource resource = new ByteArrayResource(imageData);
                    
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getFileName() + "\"")
                            .contentType(MediaType.parseMediaType(image.getContentType()))
                            .body(resource);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get user's profile picture (authenticated users only)
     * Users can only view their own profile pictures, admins can view any.
     */
    @GetMapping("/profile/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<ImageResponse> getUserProfilePicture(@PathVariable Long userId, 
                                                              @RequestAttribute("currentUser") User currentUser) {
        log.info("Fetching profile picture for user: {} by user: {}", userId, currentUser.getId());
        
        // Users can only view their own profile pictures, admins can view any
        if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getId().equals(userId)) {
            throw new SecurityException("Access denied. Users can only view their own profile pictures.");
        }
        
        return imageService.getUserProfilePicture(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all images by type
     * Requires ADMIN or MODERATOR role.
     */
    @GetMapping("/type/{imageType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<ImageResponse>> getImagesByType(@PathVariable ImageType imageType) {
        log.info("Fetching images by type: {}", imageType);
        List<ImageResponse> images = imageService.getImagesByType(imageType);
        return ResponseEntity.ok(images);
    }

    /**
     * Get all images with pagination
     * Requires ADMIN or MODERATOR role.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Page<ImageResponse>> getAllImages(Pageable pageable) {
        log.info("Fetching all images with pagination");
        Page<ImageResponse> images = imageService.getAllImages(pageable);
        return ResponseEntity.ok(images);
    }

    /**
     * Get image statistics for a user
     * Requires authenticated user (can view their own statistics).
     */
    @GetMapping("/user/{uploaderId}/stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<ImageStatsResponse> getImageStats(@PathVariable Long uploaderId) {
        log.info("Fetching image statistics for uploader: {}", uploaderId);
        long totalImages = imageService.countImagesByUploaderId(uploaderId);
        long totalStorage = imageService.getTotalStorageUsedByUploaderId(uploaderId);
        
        ImageStatsResponse stats = ImageStatsResponse.builder()
                .uploaderId(uploaderId)
                .totalImages(totalImages)
                .totalStorageBytes(totalStorage)
                .build();
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Get image statistics by type
     * Requires ADMIN or MODERATOR role.
     */
    @GetMapping("/type/{imageType}/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ImageTypeStatsResponse> getImageTypeStats(@PathVariable ImageType imageType) {
        log.info("Fetching image statistics for type: {}", imageType);
        long totalImages = imageService.countImagesByType(imageType);
        
        ImageTypeStatsResponse stats = ImageTypeStatsResponse.builder()
                .imageType(imageType)
                .totalImages(totalImages)
                .build();
        
        return ResponseEntity.ok(stats);
    }
} 