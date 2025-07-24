package com.codehacks.post.controller;

import com.codehacks.post.model.Post;
import com.codehacks.post.service.PostService;
import com.codehacks.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<Post>> getAllPublishedPosts(@RequestParam(required = false) String query) {
        List<Post> posts;
        if (query != null && !query.trim().isEmpty()) {
            posts = postService.searchPosts(query);
        } else {
            posts = postService.getAllPublishedPosts();
        }
        return ResponseEntity.ok(posts);
    }

    // Get a single post by ID (accessible to all, but draft posts need authorization)
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Post post = postService.getPostById(id)
                .orElseThrow(() -> new NoSuchElementException("Post not found with ID: " + id));

        // Logic to restrict draft post access
//        if (post.getStatus() == PostStatus.DRAFT) {
//            // Only admin or the author can view draft posts
//            if (currentUser == null || (!currentUser.getRole().equals(User.UserRole.ADMIN) && !post.getAuthorId().equals(currentUser.getId()))) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//            }
//        }
        return ResponseEntity.ok(post);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Post> createPost(@Valid @RequestBody Post post, @AuthenticationPrincipal User currentUser) {
        post.setAuthorId(currentUser.getId());
        Post createdPost = postService.createPost(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @postService.getPostById(#id).orElse(null)?.authorId == authentication.principal.id")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @Valid @RequestBody Post post, @AuthenticationPrincipal User currentUser) {
        // Ensure the authorId is not changed via update, it's set on creation
        post.setAuthorId(currentUser.getId()); // Ensure the author ID from the request body is ignored or validated

        Post updatedPost = postService.updatePost(id, post);
        return ResponseEntity.ok(updatedPost);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{id}/clap")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> clapForPost(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        postService.clapForPost(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/clap")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unclapForPost(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        postService.unclapForPost(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/claps/count")
    public ResponseEntity<Long> getClapCount(@PathVariable Long id) {
        long count = postService.getClapCountForPost(id);
        return ResponseEntity.ok(count);
    }


    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }


    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
