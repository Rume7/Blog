package com.codehacks.comment.model;

/**
 * Enum representing the status of a comment in the moderation system.
 */
public enum CommentStatus {
    /**
     * Comment is pending moderation review
     */
    PENDING,
    
    /**
     * Comment has been approved and is visible
     */
    APPROVED,
    
    /**
     * Comment has been rejected as spam or inappropriate
     */
    SPAM,
    
    /**
     * Comment has been deleted/removed
     */
    DELETED
} 