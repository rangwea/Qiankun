package com.wikia.calabash.cache;

import lombok.Data;

@Data
public class CacheValueWrapper {
    private boolean hasValue;
    private Object value;
}
