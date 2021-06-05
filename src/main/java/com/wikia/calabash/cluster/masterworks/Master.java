package com.wikia.calabash.cluster.masterworks;

import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

/**
 * @author wikia
 * @since 5/27/2021 3:23 PM
 */
public interface Master {
    void start();

    void stop();

    boolean isStart();

    default void workersChange(PathChildrenCacheEvent event) {}
}
