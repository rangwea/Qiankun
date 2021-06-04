package com.wikia.calabash;

/**
 * @author wikia
 * @since 6/1/2021 11:11 AM
 */
public class Defaults {

    public static <T> T ifNull(T currentValue, T defaultValue) {
        return currentValue == null ? defaultValue : currentValue;
    }

}
