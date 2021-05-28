package com.wikia.calabash.batch;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author wikia
 * @since 1/13/2021 5:34 PM
 */
@Setter
@Getter
@ToString
public class RetryRecord<T> {
    // 写入重试队列时间
    private long timestamp;
    // 重试次数
    private int retryTime = 1;
    private T data;

    /**
     * not safe
     */
    public void retryIncrement() {
        retryTime++;
    }
}
