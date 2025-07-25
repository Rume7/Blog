package com.codehacks.post.repository;

import com.codehacks.post.model.Clap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClapRepository extends JpaRepository<Clap, Long> {

    // Check if a user has already clapped for a specific post
    Optional<Clap> findByUserIdAndPostId(Long userId, Long postId);

    // Count claps for a specific post
    long countByPostId(Long postId);

}
