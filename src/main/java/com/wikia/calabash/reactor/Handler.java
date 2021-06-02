package com.wikia.calabash.reactor;

/**
 * @author feijianwu
 * @since 5/20/2021 2:58 PM
 */
public interface Handler<T> {
    void handle(T t);
}
