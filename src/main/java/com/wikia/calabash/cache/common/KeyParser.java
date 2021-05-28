package com.wikia.calabash.cache.common;

import java.lang.reflect.Method;

/**
 * @author wikia
 * @since 2020/3/11 19:40
 */
public interface KeyParser {
    String generateKey(String name, String key, Method method, Object[] args);

    Object[] parseKey(String key, Method method);
}
