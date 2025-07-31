package com.codehacks.image.service;

import com.codehacks.image.dto.ImageResponse;
import com.codehacks.image.model.Image;
import com.codehacks.image.model.ImageType;
import com.codehacks.image.repository.ImageRepository;
import com.codehacks.user.model.User;
import com.codehacks.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for image management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Value("${app.image.upload.path:uploads/images}")
    private String uploadPath;

    @Value("${app.image.max.size:5242880}") // 5MB default
    private long maxFileSize;

    @Value("${app.image.max.width:1920}")
    private int maxWidth;

    @Value("${app.image.max.height:1080}")
    private int maxHeight;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    @Override
    @Transactional
    public ImageResponse uploadImage(MultipartFile file, ImageType imageType, String altText, String description, Long uploaderId) {
        log.info("Uploading image for user {} with type {}", uploaderId, imageType);

        // Validate file
        if (!isValidImageFile(file)) {
            throw new IllegalArgumentException("Invalid image file");
        }

        // Get user
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + uploaderId));

        try {
            // Create upload directory if it doesn't exist
            createUploadDirectory();

            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String fileExtension = FilenameUtils.getExtension(originalFileName);
            String storedFileName = generateUniqueFileName(fileExtension);

            // Process and save image
            BufferedImage processedImage = processImage(file);
            String filePath = saveImageToDisk(processedImage, storedFileName, fileExtension);

            // Get image dimensions
            int width = processedImage.getWidth();
            int height = processedImage.getHeight();

            // Create image entity
            Image image = Image.builder()
                    .fileName(originalFileName)
                    .storedFileName(storedFileName)
                    .filePath(filePath)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .width(width)
                    .height(height)
                    .imageType(imageType)
                    .uploader(uploader)
                    .altText(altText)
                    .description(description)
                    .isActive(true)
                    .build();

            Image savedImage = imageRepository.save(image);
            log.info("Image uploaded successfully with ID: {}", savedImage.getId());

            return ImageResponse.fromEntity(savedImage);

        } catch (IOException e) {
            log.error("Error uploading image: {}", e.getMessage());
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    @Override
    @Transactional
    public ImageResponse uploadProfilePicture(MultipartFile file, String altText, String description, Long userId) {
        log.info("Uploading profile picture for user {}", userId);

        // Validate file
        if (!isValidImageFile(file)) {
            throw new IllegalArgumentException("Invalid image file");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));

        try {
            // Create upload directory if it doesn't exist
            createUploadDirectory();

            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String fileExtension = FilenameUtils.getExtension(originalFileName);
            String storedFileName = generateUniqueFileName(fileExtension);

            // Process and save image
            BufferedImage processedImage = processImage(file);
            String filePath = saveImageToDisk(processedImage, storedFileName, fileExtension);

            // Get image dimensions
            int width = processedImage.getWidth();
            int height = processedImage.getHeight();

            // Deactivate any existing profile pictures for this user
            // Since we removed the repository method, we'll use a different approach
            // We can either: 1) Store the profile picture ID in the User entity, or 2) Use a custom query
            // For now, let's assume the user can only have one active profile picture at a time
            // and we'll handle this by updating the user's profile picture URL directly

            // Create new profile picture image entity
            Image image = Image.builder()
                    .fileName(originalFileName)
                    .storedFileName(storedFileName)
                    .filePath(filePath)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .width(width)
                    .height(height)
                    .imageType(ImageType.PROFILE_PICTURE)
                    .uploader(user)
                    .altText(altText)
                    .description(description)
                    .isActive(true)
                    .build();

            Image savedImage = imageRepository.save(image);

            // Update user's profile picture information
            user.setProfilePictureUrl(filePath);
            user.setProfilePictureFilename(originalFileName);
            userRepository.save(user);

            log.info("Profile picture uploaded successfully with ID: {} for user {}", savedImage.getId(), userId);

            return ImageResponse.fromEntity(savedImage);

        } catch (IOException e) {
            log.error("Error uploading profile picture: {}", e.getMessage());
            throw new RuntimeException("Failed to upload profile picture", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ImageResponse> getImageById(Long id) {
        log.info("Fetching image by ID: {}", id);
        return imageRepository.findById(id)
                .filter(Image::getIsActive)
                .map(ImageResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageResponse> getImagesByType(ImageType imageType) {
        log.info("Fetching images by type: {}", imageType);
        return imageRepository.findByImageTypeAndIsActiveTrueOrderByCreatedAtDesc(imageType)
                .stream()
                .map(ImageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ImageResponse> getAllImages(Pageable pageable) {
        log.info("Fetching all images with pagination");
        return imageRepository.findByIsActiveTrue(pageable)
                .map(ImageResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getImageFile(Long id) {
        log.info("Fetching image file for ID: {}", id);
        Image image = imageRepository.findById(id)
                .filter(Image::getIsActive)
                .orElseThrow(() -> new NoSuchElementException("Image not found with ID: " + id));

        try {
            Path path = Paths.get(image.getFilePath());
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Error reading image file: {}", e.getMessage());
            throw new RuntimeException("Failed to read image file", e);
        }
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            log.warn("File size {} exceeds maximum allowed size {}", file.getSize(), maxFileSize);
            return false;
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn("Invalid content type: {}", contentType);
            return false;
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return false;
        }

        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        return List.of("jpg", "jpeg", "png", "gif", "webp").contains(extension);
    }

    @Override
    @Transactional(readOnly = true)
    public long countImagesByUploaderId(Long uploaderId) {
        log.info("Counting images for uploader: {}", uploaderId);
        return imageRepository.countByUploaderIdAndIsActiveTrue(uploaderId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countImagesByType(ImageType imageType) {
        log.info("Counting images by type: {}", imageType);
        return imageRepository.countByImageTypeAndIsActiveTrue(imageType);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalStorageUsedByUploaderId(Long uploaderId) {
        log.info("Calculating total storage used by uploader: {}", uploaderId);
        Long totalStorage = imageRepository.getTotalStorageUsedByUploaderId(uploaderId);
        return totalStorage != null ? totalStorage : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ImageResponse> getUserProfilePicture(Long userId) {
        log.info("Fetching profile picture for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        if (user.getProfilePictureUrl() == null || user.getProfilePictureUrl().isEmpty()) {
            return Optional.empty();
        }
        
        // For now, we'll return a simple response indicating the profile picture exists
        // In a real implementation, you might want to store the image ID in the User entity
        ImageResponse profilePicture = ImageResponse.builder()
                .id(0L) // Placeholder - in real implementation, extract from URL or store ID
                .fileName(user.getProfilePictureFilename())
                .filePath(user.getProfilePictureUrl())
                .imageType(ImageType.PROFILE_PICTURE)
                .uploaderId(userId)
                .uploaderName(user.getFirstName() + " " + user.getLastName())
                .isActive(true)
                .build();
        
        return Optional.of(profilePicture);
    }

    private void createUploadDirectory() throws IOException {
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            log.info("Created upload directory: {}", uploadPath);
        }
    }

    private String generateUniqueFileName(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s.%s", timestamp, uuid, extension);
    }

    private BufferedImage processImage(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        if (originalImage == null) {
            throw new IllegalArgumentException("Unable to read image file");
        }

        // Resize image if it exceeds maximum dimensions
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            log.info("Resizing image from {}x{} to fit within {}x{}", originalWidth, originalHeight, maxWidth, maxHeight);
            
            return Thumbnails.of(originalImage)
                    .size(maxWidth, maxHeight)
                    .keepAspectRatio(true)
                    .asBufferedImage();
        }

        return originalImage;
    }

    private String saveImageToDisk(BufferedImage image, String fileName, String extension) throws IOException {
        String filePath = Paths.get(uploadPath, fileName).toString();
        File outputFile = new File(filePath);
        
        // Ensure parent directory exists
        outputFile.getParentFile().mkdirs();
        
        // Save image
        ImageIO.write(image, extension, outputFile);
        log.info("Image saved to disk: {}", filePath);
        
        return filePath;
    }
} 