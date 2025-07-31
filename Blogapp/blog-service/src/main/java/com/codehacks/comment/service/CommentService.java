package com.codehacks.comment.service;

import com.codehacks.comment.dto.CommentModerationRequest;
import com.codehacks.comment.dto.CommentRequest;
import com.codehacks.comment.dto.CommentResponse;
import com.codehacks.comment.dto.CommentStatistics;
import com.codehacks.comment.model.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for comment management operations.
 */
public interface CommentService {

    /**
     * Create a new comment
     */
    CommentResponse createComment(CommentRequest request, Long authorId);

    /**
     * Get a comment by ID
     */
    Optional<CommentResponse> getCommentById(Long id);

    /**
     * Get all approved comments for a post
     */
    List<CommentResponse> getApprovedCommentsByPostId(Long postId);

    /**
     * Get all comments for a post (including pending, for admins)
     */
    List<CommentResponse> getAllCommentsByPostId(Long postId);

    /**
     * Get comments by post ID with pagination
     */
    Page<CommentResponse> getCommentsByPostIdWithPagination(Long postId, Pageable pageable);

    /**
     * Get all replies for a comment
     */
    List<CommentResponse> getRepliesByCommentId(Long commentId);

    /**
     * Get all comments by a user
     */
    List<CommentResponse> getCommentsByUserId(Long userId);

    /**
     * Get all comments by status
     */
    List<CommentResponse> getCommentsByStatus(CommentStatus status);

    /**
     * Get all pending comments for moderation
     */
    List<CommentResponse> getPendingCommentsForModeration();

    /**
     * Update a comment
     */
    CommentResponse updateComment(Long id, CommentRequest request, Long authorId);

    /**
     * Delete a comment (soft delete)
     */
    void deleteComment(Long id, Long authorId);

    /**
     * Moderate a comment (approve, reject, mark as spam)
     */
    CommentResponse moderateComment(Long id, CommentModerationRequest request, Long moderatorId);

    /**
     * Get comment statistics
     */
    CommentStatistics getCommentStatistics();

    /**
     * Get comments created after a specific date
     */
    List<CommentResponse> getCommentsCreatedAfter(java.time.LocalDateTime since);

    /**
     * Search comments by keyword
     */
    List<CommentResponse> searchComments(String keyword);

    /**
     * Check if a user has commented on a post
     */
    boolean hasUserCommentedOnPost(Long userId, Long postId);

    /**
     * Get the most recent comment by a user
     */
    Optional<CommentResponse> getMostRecentCommentByUserId(Long userId);

    /**
     * Count comments by status
     */
    long countCommentsByStatus(CommentStatus status);

    /**
     * Count comments for a specific post
     */
    long countCommentsByPostId(Long postId);
} 