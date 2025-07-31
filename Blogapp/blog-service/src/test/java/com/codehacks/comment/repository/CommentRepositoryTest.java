package com.codehacks.comment.repository;

import com.codehacks.comment.model.Comment;
import com.codehacks.comment.model.CommentStatus;
import com.codehacks.post.model.Post;
import com.codehacks.post.model.PostStatus;
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
class CommentRepositoryTest {

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
    private CommentRepository commentRepository;

    @Autowired
    private com.codehacks.post.repository.PostRepository postRepository;

    @Autowired
    private com.codehacks.user.repository.UserRepository userRepository;

    private User testUser;
    private Post testPost;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .username("testuser")
                .build();
        testUser = userRepository.save(testUser);

        // Create test post
        testPost = new Post();
        testPost.setTitle("Test Post");
        testPost.setContent("Test content");
        testPost.setStatus(PostStatus.PUBLISHED);
        testPost.setAuthorId(testUser.getId());
        testPost = postRepository.save(testPost);

        // Create test comment
        testComment = Comment.builder()
                .content("Test comment content")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();
        testComment = commentRepository.save(testComment);
    }

    @Test
    void shouldSaveComment() {
        // Given
        Comment comment = Comment.builder()
                .content("New comment")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.PENDING)
                .build();

        // When
        Comment savedComment = commentRepository.save(comment);

        // Then
        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getContent()).isEqualTo("New comment");
        assertThat(savedComment.getStatus()).isEqualTo(CommentStatus.PENDING);
    }

    @Test
    void shouldFindApprovedCommentsByPostId() {
        // Given - Create additional comments
        Comment approvedComment = Comment.builder()
                .content("Approved comment")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.APPROVED)
                .build();
        commentRepository.save(approvedComment);

        Comment pendingComment = Comment.builder()
                .content("Pending comment")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.PENDING)
                .build();
        commentRepository.save(pendingComment);

        // When
        List<Comment> approvedComments = commentRepository.findApprovedCommentsByPostId(testPost.getId());

        // Then
        assertThat(approvedComments).hasSize(2);
        assertThat(approvedComments).allMatch(comment -> comment.getStatus() == CommentStatus.APPROVED);
        assertThat(approvedComments).allMatch(comment -> comment.getPost().getId().equals(testPost.getId()));
    }

    @Test
    void shouldFindAllCommentsByPostId() {
        // Given - Create comments with different statuses
        Comment approvedComment = Comment.builder()
                .content("Approved comment")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.APPROVED)
                .build();
        commentRepository.save(approvedComment);

        Comment pendingComment = Comment.builder()
                .content("Pending comment")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.PENDING)
                .build();
        commentRepository.save(pendingComment);

        // When
        List<Comment> allComments = commentRepository.findAllCommentsByPostId(testPost.getId());

        // Then
        assertThat(allComments).hasSize(3); // Including the testComment from setUp
        assertThat(allComments).allMatch(comment -> comment.getPost().getId().equals(testPost.getId()));
    }

    @Test
    void shouldFindCommentsByUserId() {
        // Given - Create another user and comment
        User anotherUser = User.builder()
                .email("another@example.com")
                .firstName("Another")
                .lastName("User")
                .role(UserRole.USER)
                .username("anotheruser")
                .build();
        anotherUser = userRepository.save(anotherUser);

        Comment anotherComment = Comment.builder()
                .content("Another user comment")
                .author(anotherUser)
                .post(testPost)
                .status(CommentStatus.APPROVED)
                .build();
        commentRepository.save(anotherComment);

        // When
        List<Comment> userComments = commentRepository.findCommentsByUserId(testUser.getId());

        // Then
        assertThat(userComments).hasSize(1);
        assertThat(userComments.get(0).getAuthor().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFindCommentsByStatus() {
        // Given - Create comments with different statuses
        Comment pendingComment = Comment.builder()
                .content("Pending comment")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.PENDING)
                .build();
        commentRepository.save(pendingComment);

        Comment spamComment = Comment.builder()
                .content("Spam comment")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.SPAM)
                .build();
        commentRepository.save(spamComment);

        // When
        List<Comment> pendingComments = commentRepository.findByStatusOrderByCreatedAtDesc(CommentStatus.PENDING);

        // Then
        assertThat(pendingComments).hasSize(1);
        assertThat(pendingComments.get(0).getStatus()).isEqualTo(CommentStatus.PENDING);
    }

    @Test
    void shouldFindPendingCommentsForModeration() {
        // Given - Create pending comments
        Comment pendingComment1 = Comment.builder()
                .content("Pending comment 1")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.PENDING)
                .build();
        commentRepository.save(pendingComment1);

        Comment pendingComment2 = Comment.builder()
                .content("Pending comment 2")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.PENDING)
                .build();
        commentRepository.save(pendingComment2);

        // When
        List<Comment> pendingComments = commentRepository.findPendingCommentsForModeration();

        // Then
        assertThat(pendingComments).hasSize(2);
        assertThat(pendingComments).allMatch(comment -> comment.getStatus() == CommentStatus.PENDING);
    }

    @Test
    void shouldCountCommentsByStatus() {
        // Given - Create comments with different statuses
        Comment pendingComment = Comment.builder()
                .content("Pending comment")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.PENDING)
                .build();
        commentRepository.save(pendingComment);

        Comment spamComment = Comment.builder()
                .content("Spam comment")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.SPAM)
                .build();
        commentRepository.save(spamComment);

        // When
        long approvedCount = commentRepository.countByStatus(CommentStatus.APPROVED);
        long pendingCount = commentRepository.countByStatus(CommentStatus.PENDING);
        long spamCount = commentRepository.countByStatus(CommentStatus.SPAM);

        // Then
        assertThat(approvedCount).isEqualTo(1); // testComment from setUp
        assertThat(pendingCount).isEqualTo(1);
        assertThat(spamCount).isEqualTo(1);
    }

    @Test
    void shouldCountCommentsByPostIdAndStatus() {
        // Given - Create another post and comment
        Post anotherPost = new Post();
        anotherPost.setTitle("Another Post");
        anotherPost.setContent("Another content");
        anotherPost.setStatus(PostStatus.PUBLISHED);
        anotherPost.setAuthorId(testUser.getId());
        anotherPost = postRepository.save(anotherPost);

        Comment anotherComment = Comment.builder()
                .content("Another post comment")
                .author(testUser)
                .post(anotherPost)
                .status(CommentStatus.APPROVED)
                .build();
        commentRepository.save(anotherComment);

        // When
        long testPostApprovedCount = commentRepository.countByPostIdAndStatus(testPost.getId(), CommentStatus.APPROVED);
        long anotherPostApprovedCount = commentRepository.countByPostIdAndStatus(anotherPost.getId(), CommentStatus.APPROVED);

        // Then
        assertThat(testPostApprovedCount).isEqualTo(1); // testComment from setUp
        assertThat(anotherPostApprovedCount).isEqualTo(1);
    }

    @Test
    void shouldFindCommentsCreatedAfter() {
        // Given - Create comments and then query for comments after a future timestamp
        Comment comment1 = Comment.builder()
                .content("Comment 1")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.APPROVED)
                .build();
        commentRepository.save(comment1);

        Comment comment2 = Comment.builder()
                .content("Comment 2")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.APPROVED)
                .build();
        commentRepository.save(comment2);

        // When - Use a timestamp in the future to ensure no comments are found
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        List<Comment> futureComments = commentRepository.findCommentsCreatedAfter(futureTime);

        // Then - Should find no comments since all were created before the future timestamp
        assertThat(futureComments).isEmpty();
    }

    @Test
    void shouldCheckIfUserCommentedOnPost() {
        // Given - Create another user
        User anotherUser = User.builder()
                .email("another@example.com")
                .firstName("Another")
                .lastName("User")
                .role(UserRole.USER)
                .username("anotheruser2")
                .build();
        anotherUser = userRepository.save(anotherUser);

        // When
        boolean testUserCommented = commentRepository.existsByPostIdAndUserId(testPost.getId(), testUser.getId());
        boolean anotherUserCommented = commentRepository.existsByPostIdAndUserId(testPost.getId(), anotherUser.getId());

        // Then
        assertThat(testUserCommented).isTrue();
        assertThat(anotherUserCommented).isFalse();
    }

    @Test
    void shouldFindCommentsByKeyword() {
        // Given - Create comments with specific keywords
        Comment javaComment = Comment.builder()
                .content("This is a comment about Java programming")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.APPROVED)
                .build();
        commentRepository.save(javaComment);

        Comment springComment = Comment.builder()
                .content("Spring Boot is amazing")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.APPROVED)
                .build();
        commentRepository.save(springComment);

        // When
        List<Comment> javaComments = commentRepository.findCommentsByKeyword("Java");
        List<Comment> springComments = commentRepository.findCommentsByKeyword("Spring");

        // Then
        assertThat(javaComments).hasSize(1);
        assertThat(javaComments.get(0).getContent()).contains("Java");
        assertThat(springComments).hasSize(1);
        assertThat(springComments.get(0).getContent()).contains("Spring");
    }
} 