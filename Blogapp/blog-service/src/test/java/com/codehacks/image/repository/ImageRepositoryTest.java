package com.codehacks.image.repository;

import com.codehacks.image.model.Image;
import com.codehacks.image.model.ImageType;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Testcontainers
class ImageRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.8-alpine")
            .withDatabaseName("blog_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private com.codehacks.user.repository.UserRepository userRepository;

    @Autowired
    private com.codehacks.post.repository.PostRepository postRepository;

    private User testUser;
    private Image testImage;

    @BeforeEach
    void setUp() {
        imageRepository.deleteAll();
        userRepository.deleteAll();
        postRepository.deleteAll();

        // Create test user
        testUser = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .username("testuser")
                .build();
        testUser = userRepository.save(testUser);

        // Create test image
        testImage = Image.builder()
                .fileName("test-image.jpg")
                .storedFileName("20241201_120000_abc123.jpg")
                .filePath("/uploads/images/20241201_120000_abc123.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .width(800)
                .height(600)
                .imageType(ImageType.PROFILE_PICTURE)
                .uploader(testUser)
                .altText("Test profile picture")
                .description("A test profile picture")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        testImage = imageRepository.save(testImage);
    }

    @Test
    void shouldSaveImage() {
        // Given
        Image image = Image.builder()
                .fileName("test-image.jpg")
                .storedFileName("20241201_120000_abc123.jpg")
                .filePath("/uploads/images/20241201_120000_abc123.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .width(800)
                .height(600)
                .imageType(ImageType.PROFILE_PICTURE)
                .uploader(testUser)
                .altText("Test image")
                .description("A test image")
                .isActive(true)
                .build();

        // When
        Image savedImage = imageRepository.save(image);

        // Then
        assertThat(savedImage.getId()).isNotNull();
        assertThat(savedImage.getFileName()).isEqualTo("test-image.jpg");
        assertThat(savedImage.getUploader().getId()).isEqualTo(testUser.getId());
        assertThat(savedImage.getImageType()).isEqualTo(ImageType.PROFILE_PICTURE);
    }

    @Test
    void shouldFindImagesByType() {
        // Given - Create images with different types
        Image featuredImage = Image.builder()
                .fileName("featured-image.jpg")
                .storedFileName("20241201_150000_jkl012.jpg")
                .filePath("/uploads/images/20241201_150000_jkl012.jpg")
                .contentType("image/jpeg")
                .fileSize(3072L)
                .width(1600)
                .height(1200)
                .imageType(ImageType.FEATURED_IMAGE)
                .uploader(testUser)
                .isActive(true)
                .build();
        imageRepository.save(featuredImage);

        // When
        List<Image> profileImages = imageRepository.findByImageTypeAndIsActiveTrueOrderByCreatedAtDesc(ImageType.PROFILE_PICTURE);
        List<Image> featuredImages = imageRepository.findByImageTypeAndIsActiveTrueOrderByCreatedAtDesc(ImageType.FEATURED_IMAGE);

        // Then
        assertThat(profileImages).hasSize(1);
        assertThat(profileImages.get(0).getImageType()).isEqualTo(ImageType.PROFILE_PICTURE);
        assertThat(featuredImages).hasSize(1);
        assertThat(featuredImages.get(0).getImageType()).isEqualTo(ImageType.FEATURED_IMAGE);
    }

    @Test
    void shouldCountImagesByUploaderId() {
        // Given - Create additional images for the same user
        Image image2 = Image.builder()
                .fileName("image2.jpg")
                .storedFileName("20241201_170000_stu901.jpg")
                .filePath("/uploads/images/20241201_170000_stu901.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .width(800)
                .height(600)
                .imageType(ImageType.FEATURED_IMAGE)
                .uploader(testUser)
                .isActive(true)
                .build();
        imageRepository.save(image2);

        // When
        long count = imageRepository.countByUploaderIdAndIsActiveTrue(testUser.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldCountImagesByType() {
        // Given - Create additional images of the same type
        Image image2 = Image.builder()
                .fileName("image2.jpg")
                .storedFileName("20241201_180000_vwx234.jpg")
                .filePath("/uploads/images/20241201_180000_vwx234.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .width(800)
                .height(600)
                .imageType(ImageType.PROFILE_PICTURE)
                .uploader(testUser)
                .isActive(true)
                .build();
        imageRepository.save(image2);

        // When
        long profileCount = imageRepository.countByImageTypeAndIsActiveTrue(ImageType.PROFILE_PICTURE);
        long featuredCount = imageRepository.countByImageTypeAndIsActiveTrue(ImageType.FEATURED_IMAGE);

        // Then
        assertThat(profileCount).isEqualTo(2);
        assertThat(featuredCount).isEqualTo(0);
    }

    @Test
    void shouldFindImagesByFileNameAndUploaderId() {
        // Given - Create another image with same filename but different uploader
        User anotherUser = User.builder()
                .email("another@example.com")
                .firstName("Another")
                .lastName("User")
                .role(UserRole.USER)
                .username("anotheruser")
                .build();
        anotherUser = userRepository.save(anotherUser);

        Image duplicateImage = Image.builder()
                .fileName("test-image.jpg") // Same filename
                .storedFileName("20241201_190000_yz0567.jpg")
                .filePath("/uploads/images/20241201_190000_yz0567.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .width(800)
                .height(600)
                .imageType(ImageType.PROFILE_PICTURE)
                .uploader(anotherUser)
                .isActive(true)
                .build();
        imageRepository.save(duplicateImage);

        // When
        List<Image> duplicates = imageRepository.findByFileNameAndUploaderIdAndIsActiveTrue("test-image.jpg", testUser.getId());

        // Then
        assertThat(duplicates).hasSize(1);
        assertThat(duplicates.get(0).getUploader().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFindImagesByContentType() {
        // Given - Create images with different content types
        Image pngImage = Image.builder()
                .fileName("test-image.png")
                .storedFileName("20241201_200000_abc890.png")
                .filePath("/uploads/images/20241201_200000_abc890.png")
                .contentType("image/png")
                .fileSize(1024L)
                .width(800)
                .height(600)
                .imageType(ImageType.PROFILE_PICTURE)
                .uploader(testUser)
                .isActive(true)
                .build();
        imageRepository.save(pngImage);

        // When
        List<Image> jpegImages = imageRepository.findByContentTypeAndIsActiveTrueOrderByCreatedAtDesc("image/jpeg");
        List<Image> pngImages = imageRepository.findByContentTypeAndIsActiveTrueOrderByCreatedAtDesc("image/png");

        // Then
        assertThat(jpegImages).hasSize(1);
        assertThat(jpegImages.get(0).getContentType()).isEqualTo("image/jpeg");
        assertThat(pngImages).hasSize(1);
        assertThat(pngImages.get(0).getContentType()).isEqualTo("image/png");
    }

    @Test
    void shouldFindLargeImages() {
        // Given - Create images with different sizes
        Image largeImage = Image.builder()
                .fileName("large-image.jpg")
                .storedFileName("20241201_210000_def123.jpg")
                .filePath("/uploads/images/20241201_210000_def123.jpg")
                .contentType("image/jpeg")
                .fileSize(10485760L) // 10MB
                .width(4000)
                .height(3000)
                .imageType(ImageType.FEATURED_IMAGE)
                .uploader(testUser)
                .isActive(true)
                .build();
        imageRepository.save(largeImage);

        // When
        List<Image> largeImages = imageRepository.findLargeImages(5242880L); // 5MB threshold

        // Then
        assertThat(largeImages).hasSize(1);
        assertThat(largeImages.get(0).getFileName()).isEqualTo("large-image.jpg");
    }
} 