package com.codehacks.image.controller;

import com.codehacks.image.dto.ImageResponse;
import com.codehacks.image.model.ImageType;
import com.codehacks.image.service.ImageService;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.codehacks.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ImageController imageController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;
    private ImageResponse testResponse;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imageController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new TestSecurityExceptionHandler())
                .build();
        objectMapper.findAndRegisterModules();

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .username("testuser")
                .build();

        testResponse = ImageResponse.builder()
                .id(1L)
                .fileName("test-image.jpg")
                .storedFileName("20241201_120000_abc123.jpg")
                .filePath("/uploads/images/20241201_120000_abc123.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .width(800)
                .height(600)
                .imageType(ImageType.PROFILE_PICTURE)
                .uploaderId(1L)
                .uploaderName("Test User")
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

    private MockHttpServletRequestBuilder addCurrentUser(MockHttpServletRequestBuilder request) {
        return request.requestAttr("currentUser", testUser);
    }

    @Test
    void shouldGetImageByIdSuccessfully() throws Exception {
        // Given - Only featured images are publicly accessible
        ImageResponse featuredImageResponse = ImageResponse.builder()
                .id(1L)
                .fileName("featured-image.jpg")
                .imageType(ImageType.FEATURED_IMAGE)
                .build();
        when(imageService.getImageById(1L)).thenReturn(Optional.of(featuredImageResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/images/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileName").value("featured-image.jpg"))
                .andExpect(jsonPath("$.imageType").value("FEATURED_IMAGE"));

        verify(imageService).getImageById(1L);
    }

    @Test
    void shouldRejectPublicAccessToProfilePicture() throws Exception {
        // Given - Profile pictures should not be publicly accessible
        when(imageService.getImageById(1L)).thenReturn(Optional.of(testResponse));

        // When & Then - This should throw a SecurityException which results in 403 Forbidden
        mockMvc.perform(get("/api/v1/images/1"))
                .andExpect(status().isForbidden());

        verify(imageService).getImageById(1L);
    }

    @Test
    void shouldGetImageFileSuccessfully() throws Exception {
        // Given
        when(imageService.getImageById(1L)).thenReturn(Optional.of(testResponse));
        // No need to stub getImageFile(1L) since it should not be called

        // When & Then
        mockMvc.perform(get("/api/v1/images/1/file"))
                .andExpect(status().isForbidden());

        verify(imageService, never()).getImageFile(1L); // Should not call getImageFile if forbidden
    }

    @Test
    void shouldGetImagesByType() throws Exception {
        // Given
        List<ImageResponse> images = List.of(testResponse);
        when(imageService.getImagesByType(ImageType.PROFILE_PICTURE)).thenReturn(images);

        // When & Then
        mockMvc.perform(addCurrentUser(get("/api/v1/images/type/PROFILE_PICTURE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].imageType").value("PROFILE_PICTURE"));

        verify(imageService).getImagesByType(ImageType.PROFILE_PICTURE);
    }

    @Test
    void shouldGetAllImagesWithPagination() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ImageResponse> imagePage = new PageImpl<>(List.of(testResponse), pageable, 1);
        when(imageService.getAllImages(any(Pageable.class))).thenReturn(imagePage);

        // When & Then - Use a simpler approach without Pageable binding issues
        mockMvc.perform(addCurrentUser(get("/api/v1/images")))
                .andExpect(status().isOk());

        verify(imageService).getAllImages(any(Pageable.class));
    }

    @Test
    void shouldGetImageStats() throws Exception {
        // Given
        when(imageService.countImagesByUploaderId(1L)).thenReturn(5L);
        when(imageService.getTotalStorageUsedByUploaderId(1L)).thenReturn(10240L);

        // When & Then
        mockMvc.perform(addCurrentUser(get("/api/v1/images/user/1/stats")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploaderId").value(1))
                .andExpect(jsonPath("$.totalImages").value(5))
                .andExpect(jsonPath("$.totalStorageBytes").value(10240));

        verify(imageService).countImagesByUploaderId(1L);
        verify(imageService).getTotalStorageUsedByUploaderId(1L);
    }

    @Test
    void shouldGetImageTypeStats() throws Exception {
        // Given
        when(imageService.countImagesByType(ImageType.PROFILE_PICTURE)).thenReturn(3L);

        // When & Then
        mockMvc.perform(addCurrentUser(get("/api/v1/images/type/PROFILE_PICTURE/stats")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageType").value("PROFILE_PICTURE"))
                .andExpect(jsonPath("$.totalImages").value(3));

        verify(imageService).countImagesByType(ImageType.PROFILE_PICTURE);
    }

    @Test
    void shouldUploadProfilePictureSuccessfully() throws Exception {
        // Given
        when(imageService.uploadProfilePicture(any(), any(), any(), eq(1L))).thenReturn(testResponse);

        // When & Then
        MockHttpServletRequestBuilder request = multipart("/api/v1/images/profile-picture")
                .file(testFile)
                .param("altText", "My profile picture")
                .param("description", "A nice profile picture");
        
        mockMvc.perform(addCurrentUser(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.imageType").value("PROFILE_PICTURE"));

        verify(imageService).uploadProfilePicture(any(), any(), any(), eq(1L));
    }

    @Test
    void shouldGetUserProfilePictureSuccessfully() throws Exception {
        // Given
        ImageResponse profilePicture = ImageResponse.builder()
                .id(1L)
                .fileName("profile.jpg")
                .filePath("/uploads/images/profile.jpg")
                .imageType(ImageType.PROFILE_PICTURE)
                .uploaderId(1L)
                .uploaderName("Test User")
                .isActive(true)
                .build();
        
        when(imageService.getUserProfilePicture(1L)).thenReturn(Optional.of(profilePicture));

        // When & Then
        mockMvc.perform(addCurrentUser(get("/api/v1/images/profile/1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageType").value("PROFILE_PICTURE"))
                .andExpect(jsonPath("$.fileName").value("profile.jpg"))
                .andExpect(jsonPath("$.uploaderId").value(1));

        verify(imageService).getUserProfilePicture(1L);
    }
}

@ControllerAdvice
class TestSecurityExceptionHandler {
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
} 