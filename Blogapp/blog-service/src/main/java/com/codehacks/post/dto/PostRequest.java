package com.codehacks.post.dto;

import com.codehacks.post.model.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String content;

    private String imageUrl;

    @NotNull
    private PostStatus status;
} 