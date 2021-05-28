package com.wikia.calabash.context;

/**
 * @author wikia
 * @since 1/22/2021 2:21 PM
 */
public class Context {

    private static final ThreadLocal<Context> LOCAL = ThreadLocal.withInitial(Context::new);

    private String name;
}
