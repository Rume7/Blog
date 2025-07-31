package com.codehacks.comment.service;

import com.codehacks.comment.dto.CommentModerationRequest;
import com.codehacks.comment.dto.CommentRequest;
import com.codehacks.comment.dto.CommentResponse;
import com.codehacks.comment.dto.CommentStatistics;
import com.codehacks.comment.model.Comment;
import com.codehacks.comment.model.CommentStatus;
import com.codehacks.comment.repository.CommentRepository;
import com.codehacks.post.model.Post;
import com.codehacks.post.model.PostStatus;
import com.codehacks.post.repository.PostRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User testUser;
    private Post testPost;
    private Comment testComment;
    private CommentRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .username("testuser")
                .build();

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test content");
        testPost.setStatus(PostStatus.PUBLISHED);
        testPost.setAuthorId(testUser.getId());

        testComment = Comment.builder()
                .id(1L)
                .content("Test comment")
                .author(testUser)
                .post(testPost)
                .status(CommentStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();

        testRequest = CommentRequest.builder()
                .content("Test comment content")
                .postId(1L)
                .build();
    }

    @Test
    void shouldCreateCommentSuccessfully() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // When
        CommentResponse response = commentService.createComment(testRequest, 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Test comment");
        assertThat(response.getAuthorId()).isEqualTo(1L);
        assertThat(response.getPostId()).isEqualTo(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldThrowExceptionWhenPostNotFound() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(testRequest, 1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Post not found with ID: 1");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(testRequest, 1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("User not found with ID: 1");
    }

    @Test
    void shouldCreateReplyCommentSuccessfully() {
        // Given
        CommentRequest replyRequest = CommentRequest.builder()
                .content("Reply comment")
                .postId(1L)
                .parentCommentId(1L)
                .build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // When
        CommentResponse response = commentService.createComment(replyRequest, 1L);

        // Then
        assertThat(response).isNotNull();
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldGetCommentByIdSuccessfully() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        // When
        Optional<CommentResponse> response = commentService.getCommentById(1L);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getContent()).isEqualTo("Test comment");
    }

    @Test
    void shouldReturnEmptyWhenCommentNotFound() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<CommentResponse> response = commentService.getCommentById(1L);

        // Then
        assertThat(response).isEmpty();
    }

    @Test
    void shouldGetApprovedCommentsByPostId() {
        // Given
        List<Comment> comments = List.of(testComment);
        when(commentRepository.findApprovedCommentsByPostId(1L)).thenReturn(comments);

        // When
        List<CommentResponse> responses = commentService.getApprovedCommentsByPostId(1L);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getContent()).isEqualTo("Test comment");
    }

    @Test
    void shouldGetCommentsByPostIdWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> commentPage = new PageImpl<>(List.of(testComment), pageable, 1);
        when(commentRepository.findApprovedCommentsByPostIdWithPagination(1L, pageable)).thenReturn(commentPage);

        // When
        Page<CommentResponse> responsePage = commentService.getCommentsByPostIdWithPagination(1L, pageable);

        // Then
        assertThat(responsePage.getContent()).hasSize(1);
        assertThat(responsePage.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldUpdateCommentSuccessfully() {
        // Given
        CommentRequest updateRequest = CommentRequest.builder()
                .content("Updated comment")
                .postId(1L)
                .build();

        testComment.setStatus(CommentStatus.PENDING); // Set to PENDING to allow updates
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // When
        CommentResponse response = commentService.updateComment(1L, updateRequest, 1L);

        // Then
        assertThat(response).isNotNull();
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingOtherUsersComment() {
        // Given
        CommentRequest updateRequest = CommentRequest.builder()
                .content("Updated comment")
                .postId(1L)
                .build();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        // When & Then
        assertThatThrownBy(() -> commentService.updateComment(1L, updateRequest, 2L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("User is not authorized to update this comment");
    }

    @Test
    void shouldDeleteCommentSuccessfully() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // When
        commentService.deleteComment(1L, 1L);

        // Then
        verify(commentRepository).save(any(Comment.class));
        assertThat(testComment.getStatus()).isEqualTo(CommentStatus.DELETED);
    }

    @Test
    void shouldModerateCommentSuccessfully() {
        // Given
        CommentModerationRequest moderationRequest = CommentModerationRequest.builder()
                .status(CommentStatus.APPROVED)
                .moderationNote("Approved by admin")
                .build();

        testComment.setStatus(CommentStatus.PENDING);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // When
        CommentResponse response = commentService.moderateComment(1L, moderationRequest, 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(testComment.getStatus()).isEqualTo(CommentStatus.APPROVED);
        assertThat(testComment.getModeratedBy()).isEqualTo(1L);
        assertThat(testComment.getModerationNote()).isEqualTo("Approved by admin");
        verify(commentRepository).save(testComment);
    }

    @Test
    void shouldGetCommentStatistics() {
        // Given
        when(commentRepository.count()).thenReturn(10L);
        when(commentRepository.countByStatus(CommentStatus.APPROVED)).thenReturn(7L);
        when(commentRepository.countByStatus(CommentStatus.PENDING)).thenReturn(2L);
        when(commentRepository.countByStatus(CommentStatus.SPAM)).thenReturn(1L);
        when(commentRepository.countByStatus(CommentStatus.DELETED)).thenReturn(0L);
        when(commentRepository.findAll()).thenReturn(List.of(testComment));
        when(commentRepository.findCommentsCreatedAfter(any(LocalDateTime.class))).thenReturn(List.of(testComment));

        // When
        CommentStatistics statistics = commentService.getCommentStatistics();

        // Then
        assertThat(statistics).isNotNull();
        assertThat(statistics.getTotalComments()).isEqualTo(10L);
        assertThat(statistics.getApprovedComments()).isEqualTo(7L);
        assertThat(statistics.getPendingComments()).isEqualTo(2L);
        assertThat(statistics.getSpamComments()).isEqualTo(1L);
        assertThat(statistics.getDeletedComments()).isEqualTo(0L);
    }

    @Test
    void shouldSearchCommentsByKeyword() {
        // Given
        List<Comment> comments = List.of(testComment);
        when(commentRepository.findCommentsByKeyword("test")).thenReturn(comments);

        // When
        List<CommentResponse> responses = commentService.searchComments("test");

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getContent()).isEqualTo("Test comment");
    }

    @Test
    void shouldCheckIfUserCommentedOnPost() {
        // Given
        when(commentRepository.existsByPostIdAndUserId(1L, 1L)).thenReturn(true);
        when(commentRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(false);

        // When
        boolean hasCommented = commentService.hasUserCommentedOnPost(1L, 1L);
        boolean hasNotCommented = commentService.hasUserCommentedOnPost(2L, 1L);

        // Then
        assertThat(hasCommented).isTrue();
        assertThat(hasNotCommented).isFalse();
    }

    @Test
    void shouldGetMostRecentCommentByUserId() {
        // Given
        when(commentRepository.findMostRecentCommentByUserId(1L)).thenReturn(Optional.of(testComment));

        // When
        Optional<CommentResponse> response = commentService.getMostRecentCommentByUserId(1L);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getContent()).isEqualTo("Test comment");
    }

    @Test
    void shouldCountCommentsByStatus() {
        // Given
        when(commentRepository.countByStatus(CommentStatus.APPROVED)).thenReturn(5L);

        // When
        long count = commentService.countCommentsByStatus(CommentStatus.APPROVED);

        // Then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    void shouldCountCommentsByPostId() {
        // Given
        when(commentRepository.countByPostIdAndStatus(1L, CommentStatus.APPROVED)).thenReturn(3L);

        // When
        long count = commentService.countCommentsByPostId(1L);

        // Then
        assertThat(count).isEqualTo(3L);
    }
} 