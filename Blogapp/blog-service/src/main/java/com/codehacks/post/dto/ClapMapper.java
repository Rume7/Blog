package com.codehacks.post.dto;

import com.codehacks.post.model.Clap;
import java.util.function.Function;

public class ClapMapper {

    public static final Function<Clap, ClapResponse> toResponse = clap -> {
        if (clap == null) return null;
        ClapResponse dto = new ClapResponse();
        dto.setId(clap.getId());
        dto.setUserId(clap.getUserId());
        dto.setPostId(clap.getPostId());
        dto.setCreatedAt(clap.getCreatedAt());
        return dto;
    };
} 