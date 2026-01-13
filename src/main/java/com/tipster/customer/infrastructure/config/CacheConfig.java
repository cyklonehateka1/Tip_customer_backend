package com.tipster.customer.infrastructure.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for the application.
 * Uses in-memory cache for simplicity. For production, consider using Redis or Caffeine.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure cache manager.
     * For production, consider using:
     * - Redis (distributed caching)
     * - Caffeine (high-performance local cache)
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("leagues");
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}
