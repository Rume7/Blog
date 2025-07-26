package com.codehacks.post.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClapResponse {

    private Long id;
    private Long userId;
    private Long postId;
    private LocalDateTime createdAt;
} 