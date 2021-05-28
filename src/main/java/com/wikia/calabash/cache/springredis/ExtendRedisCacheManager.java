package com.wikia.calabash.cache.springredis;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;

public class ExtendRedisCacheManager extends RedisCacheManager {
    private static final String NAME_TTL_SEPARATOR = "#";

    public ExtendRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        String[] nameAndTTL = name.split(NAME_TTL_SEPARATOR);
        String realName = nameAndTTL[0];
        if (nameAndTTL.length == 2) {
            Duration ttl = Duration.parse(nameAndTTL[1]);
            cacheConfig.entryTtl(ttl);
        }
        return super.createRedisCache(realName, cacheConfig);
    }

}
