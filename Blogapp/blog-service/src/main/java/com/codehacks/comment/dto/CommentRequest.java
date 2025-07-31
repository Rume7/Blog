package com.codehacks.comment.dto;

import com.codehacks.comment.model.Comment;
import com.codehacks.comment.model.CommentStatus;
import com.codehacks.post.model.Post;
import com.codehacks.user.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for handling comment creation and update requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 2000, message = "Comment content must be between 1 and 2000 characters")
    private String content;

    @NotNull(message = "Post ID is required")
    private Long postId;

    private Long parentCommentId; // For replies

    /**
     * Convert CommentRequest to Comment entity
     */
    public static Comment toEntity(CommentRequest request, User author, Post post, Comment parentComment) {
        return Comment.builder()
                .content(request.getContent())
                .author(author)
                .post(post)
                .parentComment(parentComment)
                .status(CommentStatus.PENDING) // Default to pending for moderation
                .build();
    }
} 