package com.codehacks.comment.service;

import com.codehacks.comment.dto.CommentModerationRequest;
import com.codehacks.comment.dto.CommentRequest;
import com.codehacks.comment.dto.CommentResponse;
import com.codehacks.comment.dto.CommentStatistics;
import com.codehacks.comment.model.Comment;
import com.codehacks.comment.model.CommentStatus;
import com.codehacks.comment.repository.CommentRepository;
import com.codehacks.post.model.Post;
import com.codehacks.post.repository.PostRepository;
import com.codehacks.user.model.User;
import com.codehacks.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of CommentService for comment management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public CommentResponse createComment(CommentRequest request, Long authorId) {
        log.info("Creating comment for post {} by user {}", request.getPostId(), authorId);

        // Validate post exists
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new NoSuchElementException("Post not found with ID: " + request.getPostId()));

        // Validate author exists
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + authorId));

        // Validate parent comment if provided
        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new NoSuchElementException("Parent comment not found with ID: " + request.getParentCommentId()));
            
            // Ensure parent comment belongs to the same post
            if (!parentComment.getPost().getId().equals(request.getPostId())) {
                throw new IllegalArgumentException("Parent comment does not belong to the specified post");
            }
        }

        // Create comment
        Comment comment = CommentRequest.toEntity(request, author, post, parentComment);
        Comment savedComment = commentRepository.save(comment);

        log.info("Comment created successfully with ID: {}", savedComment.getId());
        return CommentResponse.fromEntity(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommentResponse> getCommentById(Long id) {
        log.debug("Fetching comment by ID: {}", id);
        return commentRepository.findById(id)
                .map(CommentResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getApprovedCommentsByPostId(Long postId) {
        log.debug("Fetching approved comments for post: {}", postId);
        return commentRepository.findApprovedCommentsByPostId(postId)
                .stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getAllCommentsByPostId(Long postId) {
        log.debug("Fetching all comments for post: {}", postId);
        return commentRepository.findAllCommentsByPostId(postId)
                .stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPostIdWithPagination(Long postId, Pageable pageable) {
        log.debug("Fetching comments for post {} with pagination", postId);
        return commentRepository.findApprovedCommentsByPostIdWithPagination(postId, pageable)
                .map(CommentResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getRepliesByCommentId(Long commentId) {
        log.debug("Fetching replies for comment: {}", commentId);
        return commentRepository.findApprovedRepliesByParentId(commentId)
                .stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByUserId(Long userId) {
        log.debug("Fetching comments by user: {}", userId);
        return commentRepository.findCommentsByUserId(userId)
                .stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByStatus(CommentStatus status) {
        log.debug("Fetching comments by status: {}", status);
        return commentRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getPendingCommentsForModeration() {
        log.debug("Fetching pending comments for moderation");
        return commentRepository.findPendingCommentsForModeration()
                .stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponse updateComment(Long id, CommentRequest request, Long authorId) {
        log.info("Updating comment {} by user {}", id, authorId);

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with ID: " + id));

        // Check if user is the author or an admin
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new SecurityException("User is not authorized to update this comment");
        }

        // Only allow updates to pending comments
        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new IllegalStateException("Cannot update comment that is not pending");
        }

        comment.setContent(request.getContent());
        comment.setStatus(CommentStatus.PENDING); // Reset to pending for re-moderation
        Comment updatedComment = commentRepository.save(comment);

        log.info("Comment updated successfully");
        return CommentResponse.fromEntity(updatedComment);
    }

    @Override
    public void deleteComment(Long id, Long authorId) {
        log.info("Deleting comment {} by user {}", id, authorId);

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with ID: " + id));

        // Check if user is the author or an admin
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new SecurityException("User is not authorized to delete this comment");
        }

        comment.setStatus(CommentStatus.DELETED);
        commentRepository.save(comment);

        log.info("Comment deleted successfully");
    }

    @Override
    public CommentResponse moderateComment(Long id, CommentModerationRequest request, Long moderatorId) {
        log.info("Moderating comment {} by moderator {}", id, moderatorId);

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with ID: " + id));

        if (!comment.canBeModerated()) {
            throw new IllegalStateException("Comment cannot be moderated in its current state");
        }

        comment.setStatus(request.getStatus());
        comment.setModeratedBy(moderatorId);
        comment.setModeratedAt(LocalDateTime.now());
        comment.setModerationNote(request.getModerationNote());

        Comment moderatedComment = commentRepository.save(comment);

        log.info("Comment moderated successfully to status: {}", request.getStatus());
        return CommentResponse.fromEntity(moderatedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentStatistics getCommentStatistics() {
        log.debug("Generating comment statistics");

        long totalComments = commentRepository.count();
        long approvedComments = commentRepository.countByStatus(CommentStatus.APPROVED);
        long pendingComments = commentRepository.countByStatus(CommentStatus.PENDING);
        long spamComments = commentRepository.countByStatus(CommentStatus.SPAM);
        long deletedComments = commentRepository.countByStatus(CommentStatus.DELETED);

        // Calculate replies count
        long totalReplies = commentRepository.findAll().stream()
                .filter(Comment::isReply)
                .count();

        // Calculate recent activity
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekAgo = today.minusDays(7);
        LocalDateTime monthAgo = today.minusDays(30);

        long commentsToday = commentRepository.findCommentsCreatedAfter(today).size();
        long commentsThisWeek = commentRepository.findCommentsCreatedAfter(weekAgo).size();
        long commentsThisMonth = commentRepository.findCommentsCreatedAfter(monthAgo).size();

        return CommentStatistics.builder()
                .totalComments(totalComments)
                .approvedComments(approvedComments)
                .pendingComments(pendingComments)
                .spamComments(spamComments)
                .deletedComments(deletedComments)
                .totalReplies(totalReplies)
                .commentsToday(commentsToday)
                .commentsThisWeek(commentsThisWeek)
                .commentsThisMonth(commentsThisMonth)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsCreatedAfter(LocalDateTime since) {
        log.debug("Fetching comments created after: {}", since);
        return commentRepository.findCommentsCreatedAfter(since)
                .stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> searchComments(String keyword) {
        log.debug("Searching comments with keyword: {}", keyword);
        return commentRepository.findCommentsByKeyword(keyword)
                .stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserCommentedOnPost(Long userId, Long postId) {
        log.debug("Checking if user {} has commented on post {}", userId, postId);
        return commentRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommentResponse> getMostRecentCommentByUserId(Long userId) {
        log.debug("Fetching most recent comment by user: {}", userId);
        return commentRepository.findMostRecentCommentByUserId(userId)
                .map(CommentResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCommentsByStatus(CommentStatus status) {
        log.debug("Counting comments by status: {}", status);
        return commentRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCommentsByPostId(Long postId) {
        log.debug("Counting comments for post: {}", postId);
        return commentRepository.countByPostIdAndStatus(postId, CommentStatus.APPROVED);
    }
} 