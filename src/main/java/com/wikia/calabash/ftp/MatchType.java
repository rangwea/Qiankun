package com.wikia.calabash.ftp;

/**
 * @author wikia
 * @since 6/4/2021 4:10 PM
 */
public enum MatchType {
    STARTS_WITH(1), ENDS_WITH(2), FULL_MATCH(3);

    private final int value;

    MatchType(int value) {
        this.value = value;
    }

    public static MatchType getByValue(int value) {
        for (MatchType matchType : values()) {
            if (matchType.value == value) {
                return matchType;
            }
        }
        throw new IllegalArgumentException("not support match type value");
    }

    public int getValue() {
        return value;
    }

}
