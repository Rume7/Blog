package com.codehacks.image.service;

import com.codehacks.image.dto.ImageResponse;
import com.codehacks.image.model.Image;
import com.codehacks.image.model.ImageType;
import com.codehacks.image.repository.ImageRepository;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.codehacks.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ImageServiceImpl imageService;

    private User testUser;
    private Image testImage;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        // Configure the service with test properties
        ReflectionTestUtils.setField(imageService, "maxFileSize", 5242880L); // 5MB
        ReflectionTestUtils.setField(imageService, "maxWidth", 1920);
        ReflectionTestUtils.setField(imageService, "maxHeight", 1080);
        ReflectionTestUtils.setField(imageService, "uploadPath", "uploads/images");

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .username("testuser")
                .build();

        testImage = Image.builder()
                .id(1L)
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

        testFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @Test
    void shouldValidateValidImageFile() {
        // Given
        MockMultipartFile validFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // When
        boolean isValid = imageService.isValidImageFile(validFile);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldRejectInvalidImageFile() {
        // Given
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        // When
        boolean isValid = imageService.isValidImageFile(invalidFile);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectNullFile() {
        // When
        boolean isValid = imageService.isValidImageFile(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectEmptyFile() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        // When
        boolean isValid = imageService.isValidImageFile(emptyFile);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldGetImageByIdSuccessfully() {
        // Given
        when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));

        // When
        Optional<ImageResponse> response = imageService.getImageById(1L);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getFileName()).isEqualTo("test-image.jpg");
        assertThat(response.get().getImageType()).isEqualTo(ImageType.PROFILE_PICTURE);
    }

    @Test
    void shouldReturnEmptyWhenImageNotFound() {
        // Given
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<ImageResponse> response = imageService.getImageById(1L);

        // Then
        assertThat(response).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenImageIsInactive() {
        // Given
        testImage.setIsActive(false);
        when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));

        // When
        Optional<ImageResponse> response = imageService.getImageById(1L);

        // Then
        assertThat(response).isEmpty();
    }

    @Test
    void shouldGetImagesByType() {
        // Given
        List<Image> images = List.of(testImage);
        when(imageRepository.findByImageTypeAndIsActiveTrueOrderByCreatedAtDesc(ImageType.PROFILE_PICTURE)).thenReturn(images);

        // When
        List<ImageResponse> responses = imageService.getImagesByType(ImageType.PROFILE_PICTURE);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getImageType()).isEqualTo(ImageType.PROFILE_PICTURE);
    }

    @Test
    void shouldGetAllImagesWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Image> imagePage = new PageImpl<>(List.of(testImage), pageable, 1);
        when(imageRepository.findByIsActiveTrue(pageable)).thenReturn(imagePage);

        // When
        Page<ImageResponse> responsePage = imageService.getAllImages(pageable);

        // Then
        assertThat(responsePage.getContent()).hasSize(1);
        assertThat(responsePage.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldCountImagesByUploaderId() {
        // Given
        when(imageRepository.countByUploaderIdAndIsActiveTrue(1L)).thenReturn(5L);

        // When
        long count = imageService.countImagesByUploaderId(1L);

        // Then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    void shouldCountImagesByType() {
        // Given
        when(imageRepository.countByImageTypeAndIsActiveTrue(ImageType.PROFILE_PICTURE)).thenReturn(3L);

        // When
        long count = imageService.countImagesByType(ImageType.PROFILE_PICTURE);

        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    void shouldGetTotalStorageUsedByUploaderId() {
        // Given
        when(imageRepository.getTotalStorageUsedByUploaderId(1L)).thenReturn(1024L);

        // When
        long totalStorage = imageService.getTotalStorageUsedByUploaderId(1L);

        // Then
        assertThat(totalStorage).isEqualTo(1024L);
    }

    @Test
    void shouldGetUserProfilePicture() {
        // Given
        User userWithProfile = User.builder()
                .id(1L)
                .firstName("Test")
                .lastName("User")
                .profilePictureUrl("/uploads/images/profile.jpg")
                .profilePictureFilename("profile.jpg")
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithProfile));

        // When
        Optional<ImageResponse> response = imageService.getUserProfilePicture(1L);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getImageType()).isEqualTo(ImageType.PROFILE_PICTURE);
        assertThat(response.get().getUploaderId()).isEqualTo(1L);
        assertThat(response.get().getUploaderName()).isEqualTo("Test User");
    }

    @Test
    void shouldReturnEmptyWhenUserHasNoProfilePicture() {
        // Given
        User userWithoutProfile = User.builder()
                .id(1L)
                .firstName("Test")
                .lastName("User")
                .profilePictureUrl(null)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithoutProfile));

        // When
        Optional<ImageResponse> response = imageService.getUserProfilePicture(1L);

        // Then
        assertThat(response).isEmpty();
    }
} 