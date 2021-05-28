package com.wikia.calabash.cluster.masterworks;

import lombok.Data;

/**
 * @author wikia
 * @since 5/20/2021 3:26 PM
 */
@Data
public class Node {
    private String id;
    private String host;
    private int port;

    public Node() {
    }

    public Node(String host, int port) {
        this.host = host;
        this.port = port;
    }
}
