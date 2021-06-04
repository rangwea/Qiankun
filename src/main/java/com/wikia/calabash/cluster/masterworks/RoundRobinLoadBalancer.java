package com.wikia.calabash.cluster.masterworks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author wikia
 * @since 6/1/2021 10:14 AM
 */
public class RoundRobinLoadBalancer {
    private final ClusterManager clusterManager;
    private final Object posMonitor = new Object();
    private Integer pos = 0;

    public RoundRobinLoadBalancer(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    public Node getServer() {
        Set<Node> workers = clusterManager.getWorkers();

        List<Node> nodes = new ArrayList<>(workers);

        Node node;
        synchronized (posMonitor) {
            if (pos >= nodes.size()) {
                pos = 0;
            }
            node = nodes.get(pos);
            pos++;
        }

        return node;
    }
}
