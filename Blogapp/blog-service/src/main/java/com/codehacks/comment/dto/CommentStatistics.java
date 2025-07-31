package com.codehacks.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for comment analytics and reporting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentStatistics {

    private Long totalComments;
    private Long approvedComments;
    private Long pendingComments;
    private Long spamComments;
    private Long deletedComments;
    private Long totalReplies;
    private Long commentsToday;
    private Long commentsThisWeek;
    private Long commentsThisMonth;
    private Double averageCommentsPerPost;
    private Long mostActivePostId;
    private String mostActivePostTitle;
    private Long mostActiveUserId;
    private String mostActiveUserName;
} 