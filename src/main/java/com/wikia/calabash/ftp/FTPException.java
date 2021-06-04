package com.wikia.calabash.ftp;

/**
 * @author wikia
 * @since 5/17/2021 2:47 PM
 */
public class FTPException extends RuntimeException {

    public FTPException(String message) {
        super(message);
    }

    public FTPException(String message, Throwable cause) {
        super(message, cause);
    }

}
