package com.codehacks.post;

import com.codehacks.TestcontainersConfig;
import com.codehacks.post.model.Post;
import com.codehacks.post.model.PostStatus;
import com.codehacks.post.service.PostService;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.codehacks.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for Post module using Testcontainers with PostgreSQL.
 * Tests end-to-end functionality with real database operations.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
@Transactional
@ActiveProfiles("test")
@ContextConfiguration(classes = TestcontainersConfig.class)
class PostIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    private User testUser;
    private User adminUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser = createTestUser("test@example.com", "testuser", UserRole.USER);
        adminUser = createTestUser("admin@example.com", "adminuser", UserRole.ADMIN);
        
        // Create test post
        testPost = createTestPost("Test Post", "This is a test post content.", testUser.getId(), PostStatus.PUBLISHED);
    }

    @AfterEach
    void tearDown() {
        // Clean up all data after each test
        // This ensures tests don't interfere with each other
    }

    @Test
    void createAndGetPost() {
        // Given
        Post post = createTestPost("Integration Test Post", "Content for integration test", testUser.getId(), PostStatus.PUBLISHED);

        // When
        Post created = postService.createPost(post);
        Optional<Post> found = postService.getPostById(created.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Integration Test Post");
        assertThat(found.get().getContent()).isEqualTo("Content for integration test");
        assertThat(found.get().getAuthorId()).isEqualTo(testUser.getId());
        assertThat(found.get().getStatus()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    void updatePost() {
        // Given
        Post post = postService.createPost(testPost);
        Post updateData = createTestPost("Updated Title", "Updated content", testUser.getId(), PostStatus.DRAFT);

        // When
        Post updated = postService.updatePost(post.getId(), updateData);

        // Then
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getContent()).isEqualTo("Updated content");
        assertThat(updated.getStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(updated.getId()).isEqualTo(post.getId());
    }

    @Test
    void deletePost() {
        // Given
        Post post = postService.createPost(testPost);
        Long postId = post.getId();

        // When
        postService.deletePost(postId);

        // Then
        Optional<Post> found = postService.getPostById(postId);
        assertThat(found).isEmpty();
    }

    @Test
    void getAllPublishedPosts() {
        // Given
        Post publishedPost1 = postService.createPost(createTestPost("Published 1", "Content 1", testUser.getId(), PostStatus.PUBLISHED));
        Post publishedPost2 = postService.createPost(createTestPost("Published 2", "Content 2", testUser.getId(), PostStatus.PUBLISHED));
        Post draftPost = postService.createPost(createTestPost("Draft Post", "Draft content", testUser.getId(), PostStatus.DRAFT));

        // When
        List<Post> publishedPosts = postService.getAllPublishedPosts();

        // Then
        assertThat(publishedPosts).hasSizeGreaterThanOrEqualTo(2);
        assertThat(publishedPosts).extracting("title").contains(publishedPost1.getTitle(), publishedPost2.getTitle());
        assertThat(publishedPosts).extracting("title").doesNotContain(draftPost.getTitle());
    }

    @Test
    void searchPosts() {
        // Given - Create posts specifically for this test
        Post javaPost = postService.createPost(createTestPost("Java Programming", "Learn Java", testUser.getId(), PostStatus.PUBLISHED));
        Post pythonPost = postService.createPost(createTestPost("Python Basics", "Learn Python", testUser.getId(), PostStatus.PUBLISHED));
        Post jsPost = postService.createPost(createTestPost("JavaScript Guide", "Learn JavaScript", testUser.getId(), PostStatus.PUBLISHED));

        // When
        List<Post> javaResults = postService.searchPosts("Java Programming");
        List<Post> pythonResults = postService.searchPosts("Python");
        List<Post> javascriptResults = postService.searchPosts("JavaScript");
        List<Post> learnResults = postService.searchPosts("Learn");

        // Then
        assertThat(javaResults).hasSize(1);
        assertThat(javaResults.get(0).getTitle()).isEqualTo(javaPost.getTitle());
        
        assertThat(pythonResults).hasSize(1);
        assertThat(pythonResults.get(0).getTitle()).isEqualTo(pythonPost.getTitle());
        
        assertThat(javascriptResults).hasSize(1);
        assertThat(javascriptResults.get(0).getTitle()).isEqualTo(jsPost.getTitle());
        
        assertThat(learnResults).hasSize(3);
    }

    @Test
    void updateNonExistentPost_shouldThrow() {
        // Given
        Post updateData = createTestPost("Updated Title", "Updated content", testUser.getId(), PostStatus.PUBLISHED);

        // When & Then
        assertThatThrownBy(() -> postService.updatePost(999L, updateData))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Post not found with ID: 999");
    }

    @Test
    void deleteNonExistentPost_shouldThrow() {
        // When & Then
        assertThatThrownBy(() -> postService.deletePost(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Post not found with ID: 999");
    }

    @Test
    void clapForPost() {
        // Given
        Post post = postService.createPost(testPost);

        // When
        postService.clapForPost(post.getId(), testUser.getId());

        // Then
        // Verify clap count
        Long clapCount = postService.getClapCountForPost(post.getId());
        assertThat(clapCount).isEqualTo(1L);
    }

    @Test
    void unclapForPost() {
        // Given
        Post post = postService.createPost(testPost);
        postService.clapForPost(post.getId(), testUser.getId());

        // When
        postService.unclapForPost(post.getId(), testUser.getId());

        // Then
        // Verify clap count is back to zero
        Long clapCount = postService.getClapCountForPost(post.getId());
        assertThat(clapCount).isEqualTo(0L);
    }

    @Test
    void clapForPostTwice_shouldThrow() {
        // Given
        Post post = postService.createPost(testPost);
        postService.clapForPost(post.getId(), testUser.getId());

        // When & Then
        assertThatThrownBy(() -> postService.clapForPost(post.getId(), testUser.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User has already clapped for this post.");
    }

    @Test
    void unclapForPostWithoutClapping_shouldThrow() {
        // Given
        Post post = postService.createPost(testPost);

        // When & Then
        assertThatThrownBy(() -> postService.unclapForPost(post.getId(), testUser.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Clap not found for user on this post.");
    }

    @Test
    void multipleUsersClapForPost() {
        // Given
        Post post = postService.createPost(testPost);
        User user2 = createTestUser("user2@example.com", "user2", UserRole.USER);
        User user3 = createTestUser("user3@example.com", "user3", UserRole.USER);

        // When
        postService.clapForPost(post.getId(), testUser.getId());
        postService.clapForPost(post.getId(), user2.getId());
        postService.clapForPost(post.getId(), user3.getId());

        // Then
        Long clapCount = postService.getClapCountForPost(post.getId());
        assertThat(clapCount).isEqualTo(3L);
    }

    @Test
    void clapForNonExistentPost_shouldThrow() {
        // When & Then
        assertThatThrownBy(() -> postService.clapForPost(999L, testUser.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Post not found with ID: 999");
    }

    @Test
    void unclapForNonExistentPost_shouldThrow() {
        // When & Then
        assertThatThrownBy(() -> postService.unclapForPost(999L, testUser.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Clap not found for user on this post.");
    }

    @Test
    void getClapCountForNonExistentPost_shouldReturnZero() {
        // When
        Long clapCount = postService.getClapCountForPost(999L);

        // Then
        assertThat(clapCount).isEqualTo(0L);
    }

    @Test
    void draftPostAccessControl() {
        // Given
        Post draftPost = postService.createPost(createTestPost("Draft Post", "Draft content",
                testUser.getId(), PostStatus.DRAFT));

        // When - Author can access draft
        Optional<Post> authorAccess = postService.getPostById(draftPost.getId());
        
        // Then
        assertThat(authorAccess).isPresent();
        assertThat(authorAccess.get().getStatus()).isEqualTo(PostStatus.DRAFT);
    }

    // Helper methods
    private User createTestUser(String email, String username, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setRole(role);
        return userService.saveUser(user);
    }

    private Post createTestPost(String title, String content, Long authorId, PostStatus status) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthorId(authorId);
        post.setStatus(status);
        post.setImageUrl("http://example.com/image.jpg");
        post.setClapsCount(0);
        return post;
    }
} 