package com.wikia.calabash.batch;

/**
 * @author wikia
 * @since 1/26/2021 11:09 AM
 */
public interface DiskRetryConsumer<T> {
    void retry(T t, DiskRetryCallback disRetryCallback);
}
