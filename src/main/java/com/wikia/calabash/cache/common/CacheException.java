package com.wikia.calabash.cache.common;

import java.lang.reflect.Method;

/**
 * @author wikia
 * @since 2020/3/17 22:46
 */
public class CacheException extends RuntimeException {
    public CacheException(String cacheName, String cacheKey, Method method, Throwable throwable) {
        super(message(cacheName, cacheKey, method), throwable);
    }

    public static String message(String cacheName, String cacheKey, Method method) {
        return String.format("cacheName={};cacheKey={};method={}", cacheName, cacheKey, method);
    }
}
