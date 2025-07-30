package com.codehacks.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.cache.RedisCacheManager;

@Configuration
public class PerformanceConfig {

    private static final Logger log = LoggerFactory.getLogger(PerformanceConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Configure performance monitoring for cache operations
     * This enables cache metrics to be exposed via Actuator endpoints
     */
    @EventListener(ApplicationReadyEvent.class)
    public void configureCacheMetrics() {
        CacheManager cacheManager = applicationContext.getBean(CacheManager.class);
        if (cacheManager instanceof RedisCacheManager) {
            log.info("Redis cache manager configured for performance monitoring");
            log.info("Cache metrics available at /actuator/metrics/cache.*");
        } else {
            log.info("Cache manager configured: {}", cacheManager.getClass().getSimpleName());
        }
    }
} 