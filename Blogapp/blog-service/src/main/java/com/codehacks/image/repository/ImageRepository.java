package com.codehacks.image.repository;

import com.codehacks.image.model.Image;
import com.codehacks.image.model.ImageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for database operations on images.
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    /**
     * Find all active images by type
     */
    List<Image> findByImageTypeAndIsActiveTrueOrderByCreatedAtDesc(ImageType imageType);

    /**
     * Find active images with pagination
     */
    Page<Image> findByIsActiveTrue(Pageable pageable);

    /**
     * Count active images by uploader
     */
    long countByUploaderIdAndIsActiveTrue(Long uploaderId);

    /**
     * Count active images by type
     */
    long countByImageTypeAndIsActiveTrue(ImageType imageType);

    /**
     * Get total storage used by uploader
     */
    @Query("SELECT SUM(i.fileSize) FROM Image i WHERE i.uploader.id = :uploaderId AND i.isActive = true")
    Long getTotalStorageUsedByUploaderId(@Param("uploaderId") Long uploaderId);

    /**
     * Find images by file name (for duplicate detection)
     */
    List<Image> findByFileNameAndUploaderIdAndIsActiveTrue(String fileName, Long uploaderId);

    /**
     * Find images created after a specific date
     */
    @Query("SELECT i FROM Image i WHERE i.createdAt >= :since AND i.isActive = true ORDER BY i.createdAt DESC")
    List<Image> findImagesCreatedAfter(@Param("since") java.time.LocalDateTime since);

    /**
     * Find images by content type
     */
    List<Image> findByContentTypeAndIsActiveTrueOrderByCreatedAtDesc(String contentType);

    /**
     * Find large images (above a certain file size)
     */
    @Query("SELECT i FROM Image i WHERE i.fileSize > :minSize AND i.isActive = true ORDER BY i.fileSize DESC")
    List<Image> findLargeImages(@Param("minSize") Long minSize);
} 