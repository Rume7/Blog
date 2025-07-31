package com.codehacks.comment.dto;

import com.codehacks.comment.model.Comment;
import com.codehacks.comment.model.CommentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for comment responses in API endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private String authorEmail;
    private Long postId;
    private String postTitle;
    private Long parentCommentId;
    private CommentStatus status;
    private Long moderatedBy;
    private LocalDateTime moderatedAt;
    private String moderationNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponse> replies;
    private boolean isReply;
    private boolean hasReplies;

    /**
     * Convert Comment entity to CommentResponse DTO
     */
    public static CommentResponse fromEntity(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getFirstName() + " " + comment.getAuthor().getLastName())
                .authorEmail(comment.getAuthor().getEmail())
                .postId(comment.getPost().getId())
                .postTitle(comment.getPost().getTitle())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .status(comment.getStatus())
                .moderatedBy(comment.getModeratedBy())
                .moderatedAt(comment.getModeratedAt())
                .moderationNote(comment.getModerationNote())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(comment.getReplies() != null ? 
                    comment.getReplies().stream()
                        .filter(reply -> reply.getStatus() == CommentStatus.APPROVED)
                        .map(CommentResponse::fromEntity)
                        .collect(Collectors.toList()) : null)
                .isReply(comment.isReply())
                .hasReplies(comment.hasReplies())
                .build();
    }
} 