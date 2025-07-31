package com.codehacks.image.model;

import com.codehacks.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing an uploaded image in the system.
 * Stores metadata about images uploaded by users for profile pictures or post featured images.
 */
@Entity
@Table(name = "images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName; // Original file name

    @Column(nullable = false)
    private String storedFileName; // Name of file as stored on disk

    @Column(nullable = false)
    private String filePath; // Path to the stored file

    @Column(nullable = false)
    private String contentType; // MIME type (e.g., image/jpeg, image/png)

    @Column(nullable = false)
    private Long fileSize; // File size in bytes

    @Column(nullable = false)
    private Integer width; // Image width in pixels

    @Column(nullable = false)
    private Integer height; // Image height in pixels

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType imageType; // PROFILE_PICTURE or FEATURED_IMAGE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader; // User who uploaded the image

    @Column(name = "alt_text")
    private String altText; // Alternative text for accessibility

    @Column(name = "description")
    private String description; // Optional description

    @Column(nullable = false)
    private Boolean isActive = true; // Soft delete flag

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
} 