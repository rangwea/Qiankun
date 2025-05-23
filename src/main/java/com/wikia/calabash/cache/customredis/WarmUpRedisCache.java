package com.wikia.calabash.cache.customredis;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface WarmUpRedisCache {
    String name();

    String key() default "";

    long expire();

    TimeUnit expireTimeUnit() default TimeUnit.SECONDS;
}
