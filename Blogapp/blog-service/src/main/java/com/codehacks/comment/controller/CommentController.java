package com.codehacks.comment.controller;

import com.codehacks.comment.dto.CommentModerationRequest;
import com.codehacks.comment.dto.CommentRequest;
import com.codehacks.comment.dto.CommentResponse;
import com.codehacks.comment.dto.CommentStatistics;
import com.codehacks.comment.model.CommentStatus;
import com.codehacks.comment.service.CommentService;
import com.codehacks.user.model.User;
import com.codehacks.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST controller for comment management operations.
 */
@RestController
@RequestMapping(Constants.COMMENTS_PATH)
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    /**
     * Create a new comment
     */
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest request,
                                                            @AuthenticationPrincipal User currentUser) {
        
        log.info("Creating comment for post {} by user {}", request.getPostId(), currentUser.getId());
        CommentResponse response = commentService.createComment(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a comment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long id) {
        log.debug("Fetching comment by ID: {}", id);
        return commentService.getCommentById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with ID: " + id));
    }

    /**
     * Get all approved comments for a post
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable Long postId) {
        log.debug("Fetching approved comments for post: {}", postId);
        List<CommentResponse> comments = commentService.getApprovedCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Get all comments for a post (including pending, for admins)
     */
    @GetMapping("/post/{postId}/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CommentResponse>> getAllCommentsByPostId(@PathVariable Long postId) {
        log.debug("Fetching all comments for post: {}", postId);
        List<CommentResponse> comments = commentService.getAllCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Get comments by post ID with pagination
     */
    @GetMapping("/post/{postId}/page")
    public ResponseEntity<Page<CommentResponse>> getCommentsByPostIdWithPagination(
            @PathVariable Long postId, Pageable pageable) {
        log.debug("Fetching comments for post {} with pagination", postId);
        Page<CommentResponse> comments = commentService.getCommentsByPostIdWithPagination(postId, pageable);
        return ResponseEntity.ok(comments);
    }

    /**
     * Get all replies for a comment
     */
    @GetMapping("/{id}/replies")
    public ResponseEntity<List<CommentResponse>> getRepliesByCommentId(@PathVariable Long id) {
        log.debug("Fetching replies for comment: {}", id);
        List<CommentResponse> replies = commentService.getRepliesByCommentId(id);
        return ResponseEntity.ok(replies);
    }

    /**
     * Get all comments by a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByUserId(@PathVariable Long userId) {
        log.debug("Fetching comments by user: {}", userId);
        List<CommentResponse> comments = commentService.getCommentsByUserId(userId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Get all comments by status (admin only)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CommentResponse>> getCommentsByStatus(@PathVariable CommentStatus status) {
        log.debug("Fetching comments by status: {}", status);
        List<CommentResponse> comments = commentService.getCommentsByStatus(status);
        return ResponseEntity.ok(comments);
    }

    /**
     * Get all pending comments for moderation (admin only)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CommentResponse>> getPendingCommentsForModeration() {
        log.debug("Fetching pending comments for moderation");
        List<CommentResponse> comments = commentService.getPendingCommentsForModeration();
        return ResponseEntity.ok(comments);
    }

    /**
     * Update a comment
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Updating comment {} by user {}", id, currentUser.getId());
        CommentResponse response = commentService.updateComment(id, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a comment
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        
        log.info("Deleting comment {} by user {}", id, currentUser.getId());
        commentService.deleteComment(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Moderate a comment (admin only)
     */
    @PutMapping("/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommentResponse> moderateComment(@PathVariable Long id,
            @Valid @RequestBody CommentModerationRequest request, @AuthenticationPrincipal User currentUser) {
        
        log.info("Moderating comment {} by admin {}", id, currentUser.getId());
        CommentResponse response = commentService.moderateComment(id, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Get comment statistics (admin only)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommentStatistics> getCommentStatistics() {
        log.debug("Fetching comment statistics");
        CommentStatistics statistics = commentService.getCommentStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get comments created after a specific date
     */
    @GetMapping("/recent")
    public ResponseEntity<List<CommentResponse>> getRecentComments(@RequestParam LocalDateTime since) {
        log.debug("Fetching comments created after: {}", since);
        List<CommentResponse> comments = commentService.getCommentsCreatedAfter(since);
        return ResponseEntity.ok(comments);
    }

    /**
     * Search comments by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<List<CommentResponse>> searchComments(@RequestParam String keyword) {
        log.debug("Searching comments with keyword: {}", keyword);
        List<CommentResponse> comments = commentService.searchComments(keyword);
        return ResponseEntity.ok(comments);
    }

    /**
     * Check if a user has commented on a post
     */
    @GetMapping("/check/{postId}")
    public ResponseEntity<Boolean> hasUserCommentedOnPost(@PathVariable Long postId, @AuthenticationPrincipal User currentUser) {
        
        log.debug("Checking if user {} has commented on post {}", currentUser.getId(), postId);
        boolean hasCommented = commentService.hasUserCommentedOnPost(currentUser.getId(), postId);
        return ResponseEntity.ok(hasCommented);
    }

    /**
     * Get the most recent comment by a user
     */
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<CommentResponse> getMostRecentCommentByUserId(@PathVariable Long userId) {
        log.debug("Fetching most recent comment by user: {}", userId);
        return commentService.getMostRecentCommentByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Count comments by status (admin only)
     */
    @GetMapping("/count/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> countCommentsByStatus(@PathVariable CommentStatus status) {
        log.debug("Counting comments by status: {}", status);
        long count = commentService.countCommentsByStatus(status);
        return ResponseEntity.ok(count);
    }

    /**
     * Count comments for a specific post
     */
    @GetMapping("/count/post/{postId}")
    public ResponseEntity<Long> countCommentsByPostId(@PathVariable Long postId) {
        log.debug("Counting comments for post: {}", postId);
        long count = commentService.countCommentsByPostId(postId);
        return ResponseEntity.ok(count);
    }
} 