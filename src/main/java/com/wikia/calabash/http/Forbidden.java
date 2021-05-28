package com.wikia.calabash.http;

/**
 * @author wikia
 * @since 2/24/2021 3:05 PM
 */
public class Forbidden extends RuntimeException {
    public Forbidden(String message) {
        super(message);
    }
}
