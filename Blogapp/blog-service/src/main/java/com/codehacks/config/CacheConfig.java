package com.codehacks.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Value("${app.cache.user.ttl:300000}")
    private long userCacheTtl;

    @Value("${app.cache.post.ttl:600000}")
    private long postCacheTtl;

    @Value("${app.cache.clap.ttl:300000}")
    private long clapCacheTtl;

    @Bean
    @Profile("!test")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        log.info("Redis template configured with JSON serialization");
        return template;
    }

    @Bean
    @Profile("!test")
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(600000)) // 10 minutes default
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // Custom cache configurations for different entities
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // User cache - shorter TTL for frequently changing data
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMillis(userCacheTtl)));
        cacheConfigurations.put("user", defaultConfig.entryTtl(Duration.ofMillis(userCacheTtl)));
        
        // Post cache - longer TTL for relatively static content
        cacheConfigurations.put("posts", defaultConfig.entryTtl(Duration.ofMillis(postCacheTtl)));
        cacheConfigurations.put("post", defaultConfig.entryTtl(Duration.ofMillis(postCacheTtl)));
        
        // Clap cache - shorter TTL for frequently updated data
        cacheConfigurations.put("claps", defaultConfig.entryTtl(Duration.ofMillis(clapCacheTtl)));
        cacheConfigurations.put("clap", defaultConfig.entryTtl(Duration.ofMillis(clapCacheTtl)));
        
        // Auth cache - very short TTL for security
        cacheConfigurations.put("auth", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();

        log.info("Cache manager configured with Redis and custom TTLs: users={}ms, posts={}ms, claps={}ms", 
                userCacheTtl, postCacheTtl, clapCacheTtl);
        
        return cacheManager;
    }

    /**
     * Simple cache manager for tests
     */
    @Bean
    @Profile("test")
    public CacheManager testCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(java.util.Arrays.asList("users", "user", "posts", "post", "claps", "clap", "auth"));
        log.info("Test cache manager configured with simple in-memory cache");
        return cacheManager;
    }
} 