package com.raf.mrworldwide.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache("amadeusToken",
                Caffeine.newBuilder().expireAfterWrite(29, TimeUnit.MINUTES).maximumSize(1).build());
        manager.registerCustomCache("stayApiDestId",
                Caffeine.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).maximumSize(500).build());
        manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(1000));
        return manager;
    }
}
