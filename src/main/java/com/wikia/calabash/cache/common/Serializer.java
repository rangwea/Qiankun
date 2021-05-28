package com.wikia.calabash.cache.common;

import java.lang.reflect.Type;

public interface Serializer {
    String serialize(Object o);

    Object deserialize(String s, Type type);
}
