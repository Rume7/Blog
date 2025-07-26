package com.codehacks.post.service;

import com.codehacks.post.model.Clap;
import com.codehacks.post.model.Post;
import com.codehacks.post.model.PostStatus;
import com.codehacks.post.repository.ClapRepository;
import com.codehacks.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ClapRepository clapRepository;

    @InjectMocks
    private PostService postService;

    private Post samplePost;

    @BeforeEach
    void setUp() {
        samplePost = new Post();
        samplePost.setId(1L);
        samplePost.setTitle("Test Post");
        samplePost.setContent("This is a test post.");
        samplePost.setAuthorId(100L);
        samplePost.setStatus(PostStatus.PUBLISHED);
        samplePost.setImageUrl("http://img");
        samplePost.setClapsCount(0);
        samplePost.setCreatedAt(LocalDateTime.now().minusDays(1));
        samplePost.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getAllPublishedPosts_shouldReturnPublishedPosts() {
        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(samplePost));

        List<Post> result = postService.getAllPublishedPosts();

        assertThat(result).hasSize(1).contains(samplePost);
        verify(postRepository).findByStatus(PostStatus.PUBLISHED);
    }

    @Test
    void searchPosts_shouldReturnMatchingPosts() {
        when(postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase("test", "test"))
                .thenReturn(List.of(samplePost));

        List<Post> result = postService.searchPosts("test");

        assertThat(result).hasSize(1).contains(samplePost);
        verify(postRepository).findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase("test", "test");
    }

    @Test
    void getPostById_shouldReturnPostIfExists() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));

        Optional<Post> result = postService.getPostById(1L);

        assertThat(result).isPresent().contains(samplePost);
        verify(postRepository).findById(1L);
    }

    @Test
    void getPostById_shouldReturnEmptyIfNotExists() {
        when(postRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Post> result = postService.getPostById(2L);

        assertThat(result).isEmpty();
        verify(postRepository).findById(2L);
    }

    @Test
    void createPost_shouldSetTimestampsAndSave() {
        Post toCreate = new Post();
        toCreate.setTitle("New");
        toCreate.setContent("Content");
        toCreate.setAuthorId(101L);
        toCreate.setStatus(PostStatus.DRAFT);
        toCreate.setImageUrl(null);
        toCreate.setClapsCount(0);

        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });

        Post result = postService.createPost(toCreate);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void updatePost_shouldUpdateFieldsAndSave() {
        Post updated = new Post();
        updated.setTitle("Updated");
        updated.setContent("Updated content");
        updated.setImageUrl("img2");
        updated.setStatus(PostStatus.PUBLISHED);

        when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        Post result = postService.updatePost(1L, updated);

        assertThat(result.getTitle()).isEqualTo("Updated");
        assertThat(result.getContent()).isEqualTo("Updated content");
        assertThat(result.getImageUrl()).isEqualTo("img2");
        assertThat(result.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        verify(postRepository).findById(1L);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void updatePost_shouldThrowIfNotFound() {
        when(postRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.updatePost(2L, samplePost))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Post not found with ID: 2");
        verify(postRepository).findById(2L);
    }

    @Test
    void deletePost_shouldDeleteIfExists() {
        when(postRepository.existsById(1L)).thenReturn(true);
        doNothing().when(postRepository).deleteById(1L);

        postService.deletePost(1L);

        verify(postRepository).existsById(1L);
        verify(postRepository).deleteById(1L);
    }

    @Test
    void deletePost_shouldThrowIfNotFound() {
        when(postRepository.existsById(2L)).thenReturn(false);

        assertThatThrownBy(() -> postService.deletePost(2L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Post not found with ID: 2");
        verify(postRepository).existsById(2L);
    }

    @Test
    void clapForPost_shouldAddClapAndIncrementCount() {
        when(clapRepository.findByUserIdAndPostId(100L, 1L)).thenReturn(Optional.empty());
        when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));
        when(clapRepository.save(any(Clap.class))).thenAnswer(inv -> inv.getArgument(0));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        postService.clapForPost(1L, 100L);

        assertThat(samplePost.getClapsCount()).isEqualTo(1);
        verify(clapRepository).findByUserIdAndPostId(100L, 1L);
        verify(clapRepository).save(any(Clap.class));
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void clapForPost_shouldThrowIfAlreadyClapped() {
        when(clapRepository.findByUserIdAndPostId(100L, 1L)).thenReturn(Optional.of(new Clap()));

        assertThatThrownBy(() -> postService.clapForPost(1L, 100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already clapped");
        verify(clapRepository).findByUserIdAndPostId(100L, 1L);
    }

    @Test
    void clapForPost_shouldThrowIfPostNotFound() {
        when(clapRepository.findByUserIdAndPostId(100L, 2L)).thenReturn(Optional.empty());
        when(postRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.clapForPost(2L, 100L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Post not found with ID: 2");
        verify(postRepository).findById(2L);
    }

    @Test
    void unclapForPost_shouldRemoveClapAndDecrementCount() {
        Clap clap = new Clap();

        when(clapRepository.findByUserIdAndPostId(100L, 1L)).thenReturn(Optional.of(clap));
        when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));
        doNothing().when(clapRepository).delete(clap);
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        samplePost.setClapsCount(2);
        postService.unclapForPost(1L, 100L);

        assertThat(samplePost.getClapsCount()).isEqualTo(1);
        verify(clapRepository).delete(clap);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void unclapForPost_shouldNotGoBelowZero() {
        Clap clap = new Clap();

        when(clapRepository.findByUserIdAndPostId(100L, 1L)).thenReturn(Optional.of(clap));
        when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));
        doNothing().when(clapRepository).delete(clap);
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        samplePost.setClapsCount(0);
        postService.unclapForPost(1L, 100L);

        assertThat(samplePost.getClapsCount()).isEqualTo(0);
    }

    @Test
    void unclapForPost_shouldThrowIfClapNotFound() {
        when(clapRepository.findByUserIdAndPostId(100L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.unclapForPost(1L, 100L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Clap not found");
    }

    @Test
    void unclapForPost_shouldThrowIfPostNotFound() {
        Clap clap = new Clap();

        when(clapRepository.findByUserIdAndPostId(100L, 2L)).thenReturn(Optional.of(clap));
        when(postRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.unclapForPost(2L, 100L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Post not found with ID: 2");
    }

    @Test
    void getClapCountForPost_shouldReturnCount() {
        when(clapRepository.countByPostId(1L)).thenReturn(5L);

        long count = postService.getClapCountForPost(1L);

        assertThat(count).isEqualTo(5L);
        verify(clapRepository).countByPostId(1L);
    }
} 