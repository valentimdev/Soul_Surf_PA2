package com.soulsurf.backend.core.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class AppCacheService {

    private final CacheManager cacheManager;

    public AppCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void clearAll() {
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }
}
