package com.wikia.calabash.cache.customredis;

import com.wikia.calabash.cache.common.CacheManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Aspect
@Slf4j
@Order(2)
public class RedisCacheAspect {
    @Resource
    private CacheManager cacheManager;

    @Around("@annotation(RedisCached)")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            RedisCached redisCached = methodSignature.getMethod().getAnnotation(RedisCached.class);

            RedisCache redisCache = cacheManager.getRedisCache(redisCached.name());
            return redisCache.computeIfAbsent(joinPoint);
        } catch (Throwable throwable) {
            log.warn("redis cache exception", throwable);
            return joinPoint.proceed();
        }
    }
}