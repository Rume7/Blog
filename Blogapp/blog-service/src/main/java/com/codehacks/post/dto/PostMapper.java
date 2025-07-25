package com.codehacks.post.dto;

import com.codehacks.post.model.Post;

import java.util.function.BiFunction;
import java.util.function.Function;

public class PostMapper {
    // Post -> PostResponse
    public static final Function<Post, PostResponse> toResponse = post -> {
        if (post == null) return null;
        PostResponse dto = new PostResponse();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setImageUrl(post.getImageUrl());
        dto.setStatus(post.getStatus());
        dto.setClapsCount(post.getClapsCount());
        dto.setAuthorId(post.getAuthorId());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        return dto;
    };

    // (PostRequest, authorId) -> Post
    public static final BiFunction<PostRequest, Long, Post> fromRequest = (req, authorId) -> {
        if (req == null || authorId == null) return null;
        Post post = new Post();
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setImageUrl(req.getImageUrl());
        post.setStatus(req.getStatus());
        post.setAuthorId(authorId);
        // clapsCount, createdAt, updatedAt handled by entity
        return post;
    };
} 