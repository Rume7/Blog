package com.codehacks.post.repository;

import com.codehacks.post.model.Post;
import com.codehacks.post.model.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Find all posts by status (e.g., PUBLISHED)
    List<Post> findByStatus(PostStatus status);

    // Find posts by title, content, or author (case-insensitive)
    // In a real app, you might join with User table for author name search
    List<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content);

    // Find posts by author ID and status
    List<Post> findByAuthorIdAndStatus(Long authorId, PostStatus status);

    // Find posts by author ID
    List<Post> findByAuthorId(Long authorId);
}
