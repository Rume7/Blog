package com.codehacks.performance;

import com.codehacks.post.model.Post;
import com.codehacks.post.model.PostStatus;
import com.codehacks.post.service.PostService;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.codehacks.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class PerformanceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.8-alpine")
            .withDatabaseName("blog_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.cache.type", () -> "redis");
        registry.add("jwt.secret", () -> "testSecretKeyForTestingPurposesOnlyThisShouldBeAtLeast256BitsLong");
    }

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private CacheManager cacheManager;

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        // Create test user with unique email for each test
        String uniqueEmail = "test" + System.currentTimeMillis() + "@example.com";
        testUser = new User();
        testUser.setEmail(uniqueEmail);
        testUser.setUsername("testuser" + System.currentTimeMillis());
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("password");
        testUser.setRole(UserRole.USER);
        testUser = userService.saveUser(testUser);

        // Create test post
        testPost = new Post();
        testPost.setTitle("Test Post");
        testPost.setContent("Test content");
        testPost.setAuthorId(testUser.getId());
        testPost.setStatus(PostStatus.PUBLISHED);
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());
        testPost = postService.createPost(testPost);
    }

    @Test
    void shouldHandleConcurrentPostReads() throws InterruptedException {
        int threadCount = 10;
        int requestsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        long startTime = System.currentTimeMillis();

        List<CompletableFuture<Void>> futures = new java.util.ArrayList<>();
        
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    postService.getPostById(testPost.getId());
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        int totalRequests = threadCount * requestsPerThread;
        double requestsPerSecond = (double) totalRequests / (totalTime / 1000.0);

        // Performance metrics for concurrent reads
        // Total requests: %d, Total time: %d ms, Requests per second: %.2f
        // These metrics are captured in the assertions below

        // Assert reasonable performance (adjust thresholds as needed)
        assertThat(requestsPerSecond).isGreaterThan(100); // At least 100 RPS
        assertThat(totalTime).isLessThan(10000); // Should complete within 10 seconds
    }

    @Test
    void shouldHandleCachePerformance() {
        // Clear cache first
        cacheManager.getCache("posts").clear();

        // First call - should hit database
        long startTime = System.currentTimeMillis();
        postService.getPostById(testPost.getId());
        long firstCallTime = System.currentTimeMillis() - startTime;

        // Second call - should hit cache
        startTime = System.currentTimeMillis();
        postService.getPostById(testPost.getId());
        long secondCallTime = System.currentTimeMillis() - startTime;

        // Cache should be significantly faster
        assertThat(secondCallTime).isLessThan(firstCallTime);
        assertThat(secondCallTime).isLessThan(100); // Cache hit should be under 100ms
    }

    @Test
    void shouldHandleBulkPostCreation() {
        int postCount = 100;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < postCount; i++) {
            Post post = new Post();
            post.setTitle("Bulk Post " + i);
            post.setContent("Bulk content " + i);
            post.setAuthorId(testUser.getId());
            post.setStatus(PostStatus.PUBLISHED);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            postService.createPost(post);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double postsPerSecond = (double) postCount / (totalTime / 1000.0);

        // Performance metrics for bulk creation
        // Posts created: %d, Total time: %d ms, Posts per second: %.2f
        // These metrics are captured in the assertions below

        // Assert reasonable performance
        assertThat(postsPerSecond).isGreaterThan(10); // At least 10 posts per second
    }
} 