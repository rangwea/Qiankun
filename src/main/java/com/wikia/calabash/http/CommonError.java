package com.wikia.calabash.http;

/**
 * @author wikia
 * @since 2019/7/23 14:47
 */
public enum CommonError {
    REQUEST_ERROR("000001", "request error"),
    SERVER_ERROR("000002", "server error")

    ;
    private final String code;
    private final String message;

    CommonError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }
}
