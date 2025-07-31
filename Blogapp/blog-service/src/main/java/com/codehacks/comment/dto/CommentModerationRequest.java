package com.codehacks.comment.dto;

import com.codehacks.comment.model.CommentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for handling comment moderation requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentModerationRequest {

    @NotNull(message = "Comment status is required")
    private CommentStatus status;

    private String moderationNote; // Optional note for the moderation action
} 