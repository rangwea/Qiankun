package com.wikia.calabash.cluster.masterworks;

import com.wikia.calabash.consistenthash.ConsistentNode;
import lombok.Data;

/**
 * @author wikia
 * @since 5/20/2021 3:26 PM
 */
@Data
public class Node implements ConsistentNode {
    private String host;
    private int port;

    public Node() {
    }

    public Node(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String getKey() {
        return host + ":" + port;
    }
}
