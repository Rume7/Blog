package com.codehacks.post.controller;

import com.codehacks.post.dto.PostRequest;
import com.codehacks.post.dto.PostResponse;
import com.codehacks.post.dto.PostMapper;
import com.codehacks.post.model.Post;
import com.codehacks.post.model.PostStatus;
import com.codehacks.post.service.PostService;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
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
    public ResponseEntity<List<PostResponse>> getAllPublishedPosts(@RequestParam(required = false) String query) {
        List<PostResponse> posts;
        if (query != null && !query.trim().isEmpty()) {
            posts = postService.searchPosts(query).stream().map(PostMapper.toResponse).toList();
        } else {
            posts = postService.getAllPublishedPosts().stream().map(PostMapper.toResponse).toList();
        }
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Post post = postService.getPostById(id)
                .orElseThrow(() -> new NoSuchElementException("Post not found with ID: " + id));

        // Enforce draft access logic
        if (post.getStatus() == PostStatus.DRAFT) {
            boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.ADMIN;
            boolean isAuthor = currentUser != null && post.getAuthorId() != null && post.getAuthorId().equals(currentUser.getId());
            if (!isAdmin && !isAuthor) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(PostMapper.toResponse.apply(post));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest postRequest,
                                                   @AuthenticationPrincipal User currentUser) {
        Post createdPost = postService.createPost(PostMapper.fromRequest.apply(postRequest, currentUser.getId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(PostMapper.toResponse.apply(createdPost));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @postService.getPostById(#id).orElse(null)?.authorId == #currentUser.id")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @Valid @RequestBody PostRequest postRequest,
                                                   @AuthenticationPrincipal User currentUser) {
        Post updatedPost = postService.updatePost(id, PostMapper.fromRequest.apply(postRequest, currentUser.getId()));
        return ResponseEntity.ok(PostMapper.toResponse.apply(updatedPost));
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
}
