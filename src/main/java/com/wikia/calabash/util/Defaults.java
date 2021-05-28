package com.wikia.calabash.util;


public class Defaults {
    public static <T> T nullDefault(T original, T defaultValue) {
        return original == null ? defaultValue : original;
    }
}
