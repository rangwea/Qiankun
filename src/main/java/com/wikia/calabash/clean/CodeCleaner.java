package com.wikia.calabash.clean;

import java.util.function.Consumer;

public class CodeCleaner {
    public static <T> void notNullCall(T o, Consumer<T> consumer) {
        if (o != null) {
            consumer.accept(o);
        }
    }
}
