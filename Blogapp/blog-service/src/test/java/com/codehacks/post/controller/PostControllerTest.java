package com.codehacks.post.controller;

import com.codehacks.post.model.Post;
import com.codehacks.post.model.PostStatus;
import com.codehacks.post.service.PostService;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

/**
 * Unit tests for PostController using Mockito.
 * Tests the controller logic in isolation without Spring context dependencies.
 */
@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    // Test data
    private Post samplePost;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Initialize test post
        samplePost = createSamplePost();
        testUser = createTestUser();
    }

    /**
     * Test that a published post is accessible without authentication.
     * Should return 200 OK for published posts.
     */
    @Test
    void getPostById_shouldReturnOkIfPublished() {
        // Given
        samplePost.setStatus(PostStatus.PUBLISHED);
        when(postService.getPostById(1L)).thenReturn(Optional.of(samplePost));

        // When
        ResponseEntity<?> response = postController.getPostById(1L, testUser);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * Test that a non-existent post returns 404.
     */
    @Test
    void getPostById_shouldReturnNotFoundIfMissing() {
        // Given
        when(postService.getPostById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postController.getPostById(999L, testUser))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Post not found with ID: 999");
    }

    /**
     * Test that a draft post is accessible by its author.
     */
    @Test
    void getPostById_shouldReturnOkIfDraftAndAuthor() {
        // Given
        samplePost.setStatus(PostStatus.DRAFT);
        samplePost.setAuthorId(testUser.getId());
        when(postService.getPostById(1L)).thenReturn(Optional.of(samplePost));

        // When
        ResponseEntity<?> response = postController.getPostById(1L, testUser);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * Test that a draft post is accessible by admin users.
     */
    @Test
    void getPostById_shouldReturnOkIfDraftAndAdmin() {
        // Given
        User adminUser = createAdminUser();
        samplePost.setStatus(PostStatus.DRAFT);
        when(postService.getPostById(1L)).thenReturn(Optional.of(samplePost));

        // When
        ResponseEntity<?> response = postController.getPostById(1L, adminUser);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * Test that a draft post is forbidden for non-author, non-admin users.
     */
    @Test
    void getPostById_shouldReturnForbiddenIfDraftAndNotAuthorOrAdmin() {
        // Given
        User otherUser = createOtherUser();
        samplePost.setStatus(PostStatus.DRAFT);
        samplePost.setAuthorId(testUser.getId()); // Different author
        when(postService.getPostById(1L)).thenReturn(Optional.of(samplePost));

        // When
        ResponseEntity<?> response = postController.getPostById(1L, otherUser);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    /**
     * Test that a draft post is forbidden for null/anonymous user.
     */
    @Test
    void getPostById_shouldReturnForbiddenIfDraftAndNoUser() {
        // Given
        samplePost.setStatus(PostStatus.DRAFT);
        samplePost.setAuthorId(testUser.getId());
        when(postService.getPostById(1L)).thenReturn(Optional.of(samplePost));

        // When
        ResponseEntity<?> response = postController.getPostById(1L, null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    /**
     * Test that a draft post with missing authorId is forbidden for non-admin users.
     */
    @Test
    void getPostById_shouldReturnForbiddenIfDraftAndMissingAuthorId() {
        // Given
        samplePost.setStatus(PostStatus.DRAFT);
        samplePost.setAuthorId(null); // Missing authorId
        when(postService.getPostById(1L)).thenReturn(Optional.of(samplePost));

        // When
        ResponseEntity<?> response = postController.getPostById(1L, testUser);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    /**
     * Test that a draft post with missing authorId is allowed for admin users.
     */
    @Test
    void getPostById_shouldReturnOkIfDraftAndMissingAuthorIdButAdmin() {
        // Given
        User adminUser = createAdminUser();
        samplePost.setStatus(PostStatus.DRAFT);
        samplePost.setAuthorId(null); // Missing authorId
        when(postService.getPostById(1L)).thenReturn(Optional.of(samplePost));

        // When
        ResponseEntity<?> response = postController.getPostById(1L, adminUser);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * Test that a user can clap for a post successfully.
     */
    @Test
    void clapForPost_shouldReturnOkWhenSuccessful() {
        // Given
        doNothing().when(postService).clapForPost(1L, testUser.getId());

        // When
        ResponseEntity<Void> response = postController.clapForPost(1L, testUser);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * Test that a user can unclap for a post successfully.
     */
    @Test
    void unclapForPost_shouldReturnOkWhenSuccessful() {
        // Given
        doNothing().when(postService).unclapForPost(1L, testUser.getId());

        // When
        ResponseEntity<Void> response = postController.unclapForPost(1L, testUser);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * Test that clapping for a non-existent post throws exception.
     */
    @Test
    void clapForPost_shouldThrowExceptionWhenPostNotFound() {
        // Given
        doThrow(new NoSuchElementException("Post not found with ID: 999"))
                .when(postService).clapForPost(999L, testUser.getId());

        // When & Then
        assertThatThrownBy(() -> postController.clapForPost(999L, testUser))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Post not found with ID: 999");
    }

    /**
     * Test that unclapping for a non-existent post throws exception.
     */
    @Test
    void unclapForPost_shouldThrowExceptionWhenPostNotFound() {
        // Given
        doThrow(new NoSuchElementException("Post not found with ID: 999"))
                .when(postService).unclapForPost(999L, testUser.getId());

        // When & Then
        assertThatThrownBy(() -> postController.unclapForPost(999L, testUser))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Post not found with ID: 999");
    }

    /**
     * Test that clapping when user already clapped throws exception.
     */
    @Test
    void clapForPost_shouldThrowExceptionWhenUserAlreadyClapped() {
        // Given
        doThrow(new IllegalStateException("User has already clapped for this post"))
                .when(postService).clapForPost(1L, testUser.getId());

        // When & Then
        assertThatThrownBy(() -> postController.clapForPost(1L, testUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User has already clapped for this post");
    }

    /**
     * Test that unclapping when user hasn't clapped throws exception.
     */
    @Test
    void unclapForPost_shouldThrowExceptionWhenUserHasNotClapped() {
        // Given
        doThrow(new IllegalStateException("User has not clapped for this post"))
                .when(postService).unclapForPost(1L, testUser.getId());

        // When & Then
        assertThatThrownBy(() -> postController.unclapForPost(1L, testUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User has not clapped for this post");
    }

    /**
     * Test that clapping with null user throws NullPointerException.
     */
    @Test
    void clapForPost_shouldThrowExceptionWhenUserIsNull() {
        // When & Then
        assertThatThrownBy(() -> postController.clapForPost(1L, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Cannot invoke \"com.codehacks.user.model.User.getId()\" because \"currentUser\" is null");
    }

    /**
     * Test that unclapping with null user throws NullPointerException.
     */
    @Test
    void unclapForPost_shouldThrowExceptionWhenUserIsNull() {
        // When & Then
        assertThatThrownBy(() -> postController.unclapForPost(1L, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Cannot invoke \"com.codehacks.user.model.User.getId()\" because \"currentUser\" is null");
    }

    /**
     * Test that getClapCount returns correct count.
     */
    @Test
    void getClapCount_shouldReturnCorrectCount() {
        // Given
        Long expectedCount = 42L;
        when(postService.getClapCountForPost(1L)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = postController.getClapCount(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedCount);
    }

    /**
     * Test that getClapCount returns zero for non-existent post.
     */
    @Test
    void getClapCount_shouldReturnZeroForNonExistentPost() {
        // Given
        when(postService.getClapCountForPost(999L)).thenReturn(0L);

        // When
        ResponseEntity<Long> response = postController.getClapCount(999L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(0L);
    }

    // Helper methods for creating test data
    private Post createSamplePost() {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");
        post.setContent("This is a test post.");
        post.setAuthorId(100L);
        post.setStatus(PostStatus.PUBLISHED);
        post.setImageUrl("http://img");
        post.setClapsCount(0);
        post.setCreatedAt(LocalDateTime.now().minusDays(1));
        post.setUpdatedAt(LocalDateTime.now());
        return post;
    }

    private User createTestUser() {
        User user = new User();
        user.setId(100L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(UserRole.USER);
        return user;
    }

    private User createAdminUser() {
        User user = new User();
        user.setId(200L);
        user.setUsername("adminuser");
        user.setEmail("admin@example.com");
        user.setRole(UserRole.ADMIN);
        return user;
    }

    private User createOtherUser() {
        User user = new User();
        user.setId(300L);
        user.setUsername("otheruser");
        user.setEmail("other@example.com");
        user.setRole(UserRole.USER);
        return user;
    }
} 