package com.codehacks.comment.repository;

import com.codehacks.comment.model.Comment;
import com.codehacks.comment.model.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for database operations on comments.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Find all approved comments for a specific post
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.status = 'APPROVED' AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findApprovedCommentsByPostId(@Param("postId") Long postId);

    /**
     * Find all comments for a specific post (including pending and approved)
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findAllCommentsByPostId(@Param("postId") Long postId);

    /**
     * Find all replies for a specific comment
     */
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId AND c.status = 'APPROVED' ORDER BY c.createdAt ASC")
    List<Comment> findApprovedRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * Find all comments by a specific user
     */
    @Query("SELECT c FROM Comment c WHERE c.author.id = :userId ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByUserId(@Param("userId") Long userId);

    /**
     * Find all comments by status
     */
    List<Comment> findByStatusOrderByCreatedAtDesc(CommentStatus status);

    /**
     * Find all pending comments that need moderation
     */
    @Query("SELECT c FROM Comment c WHERE c.status = 'PENDING' ORDER BY c.createdAt ASC")
    List<Comment> findPendingCommentsForModeration();

    /**
     * Count comments by status
     */
    long countByStatus(CommentStatus status);

    /**
     * Count comments for a specific post
     */
    long countByPostIdAndStatus(Long postId, CommentStatus status);

    /**
     * Find comments created after a specific date
     */
    @Query("SELECT c FROM Comment c WHERE c.createdAt >= :since ORDER BY c.createdAt DESC")
    List<Comment> findCommentsCreatedAfter(@Param("since") LocalDateTime since);

    /**
     * Find comments by post ID with pagination
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.status = 'APPROVED' AND c.parentComment IS NULL")
    Page<Comment> findApprovedCommentsByPostIdWithPagination(@Param("postId") Long postId, Pageable pageable);

    /**
     * Check if a user has commented on a specific post
     */
    @Query("SELECT COUNT(c) > 0 FROM Comment c WHERE c.post.id = :postId AND c.author.id = :userId")
    boolean existsByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * Find the most recent comment by a user
     */
    @Query("SELECT c FROM Comment c WHERE c.author.id = :userId ORDER BY c.createdAt DESC")
    Optional<Comment> findMostRecentCommentByUserId(@Param("userId") Long userId);

    /**
     * Find comments that contain specific keywords (for search functionality)
     */
    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:keyword% AND c.status = 'APPROVED' ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByKeyword(@Param("keyword") String keyword);
} 