package com.codehacks.post.dto;

import com.codehacks.post.model.PostStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private PostStatus status;
    private int clapsCount;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 