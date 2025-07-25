package com.codehacks.post.service;

import com.codehacks.post.model.Clap;
import com.codehacks.post.model.Post;
import com.codehacks.post.model.PostStatus;
import com.codehacks.post.repository.ClapRepository;
import com.codehacks.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Service for post-related business logic. All methods work with Post entities.
 * Controllers are responsible for mapping to/from DTOs using PostMapper.
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final ClapRepository clapRepository;


    public List<Post> getAllPublishedPosts() {
        return postRepository.findByStatus(PostStatus.PUBLISHED);
    }

    public List<Post> searchPosts(String query) {
        // In a real scenario, you might want to search only PUBLISHED posts
        // For simplicity, this searches all posts
        return postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query);
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    @Transactional
    public Post createPost(Post post) {
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        // Claps count defaults to 0 in entity, no need to set here
        Post saved = postRepository.save(post);
        log.info("Created post with id={} by authorId={}", saved.getId(), saved.getAuthorId());
        return saved;
    }

    @Transactional
    public Post updatePost(Long id, Post updatedPost) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Attempted to update non-existent post with id={}", id);
                    return new NoSuchElementException("Post not found with ID: " + id);
                });

        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());
        existingPost.setImageUrl(updatedPost.getImageUrl());
        existingPost.setStatus(updatedPost.getStatus());
        existingPost.setUpdatedAt(LocalDateTime.now());

        // If status changes to PUBLISHED, set publishedAt (if you add this field to Post)
        // For now, we only have createdAt/updatedAt. If you need a distinct 'publishedAt', add it to Post model.
        // if (updatedPost.getStatus() == PostStatus.PUBLISHED && existingPost.getStatus() != PostStatus.PUBLISHED) {
        //     existingPost.setPublishedAt(LocalDateTime.now());
        // }

        Post saved = postRepository.save(existingPost);
        log.info("Updated post with id={}", saved.getId());
        return saved;
    }

    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent post with id={}", id);
            throw new NoSuchElementException("Post not found with ID: " + id);
        }
        // Optionally delete associated claps first if not using cascade delete
        // clapRepository.deleteByPostId(id); // You'd need to add this method to ClapRepository
        postRepository.deleteById(id);
        log.info("Deleted post with id={}", id);
    }

    // --- Clap Operations ---

    @Transactional
    public void clapForPost(Long postId, Long userId) {
        // Check if the user has already clapped for this post
        Optional<Clap> existingClap = clapRepository.findByUserIdAndPostId(userId, postId);

        if (existingClap.isPresent()) {
            log.warn("User {} already clapped for post {}", userId, postId);
            // User has already clapped, you might throw an exception or just do nothing
            throw new IllegalStateException("User has already clapped for this post.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found with ID: " + postId));

        // Create a new clap entry
        Clap newClap = new Clap();
        newClap.setPostId(postId);
        newClap.setUserId(userId);
        clapRepository.save(newClap);

        // Increment claps count on the post
        post.setClapsCount(post.getClapsCount() + 1);
        postRepository.save(post);
        log.info("User {} clapped for post {}", userId, postId);
    }

    @Transactional
    public void unclapForPost(Long postId, Long userId) {
        Clap existingClap = clapRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> {
                    log.warn("User {} tried to unclap post {} but no clap exists", userId, postId);
                    return new NoSuchElementException("Clap not found for user on this post.");
                });

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found with ID: " + postId));

        clapRepository.delete(existingClap);

        // Decrement claps count on the post, ensuring it doesn't go below zero
        post.setClapsCount(Math.max(0, post.getClapsCount() - 1));
        postRepository.save(post);
        log.info("User {} unclapped post {}", userId, postId);
    }

    public long getClapCountForPost(long postId) {
        return clapRepository.countByPostId(postId);
    }
}