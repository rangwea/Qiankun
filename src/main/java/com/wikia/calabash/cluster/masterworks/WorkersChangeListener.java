package com.wikia.calabash.cluster.masterworks;

/**
 * @author wikia
 * @since 5/27/2021 3:33 PM
 */
public interface WorkersChangeListener {
    void add(Node workerInfo);

    void delete(Node workerInfo);
}
