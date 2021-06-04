package com.wikia.calabash.cluster.masterworks;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wikia
 * @since 5/31/2021 4:43 PM
 */
public class ClusterManager {
    private boolean localIsLeader;
    private Node localNode;
    private Node leaderNode;
    private Set<Node> workers = new HashSet<>();

    public boolean isLocalIsLeader() {
        return localIsLeader;
    }

    public ClusterManager setLocalIsLeader(boolean localIsLeader) {
        this.localIsLeader = localIsLeader;
        return this;
    }

    public Node getLocalNode() {
        return localNode;
    }

    public ClusterManager setLocalNode(Node localNode) {
        this.localNode = localNode;
        return this;
    }

    public Node getLeaderNode() {
        return leaderNode;
    }

    public ClusterManager setLeaderNode(Node leaderNode) {
        this.leaderNode = leaderNode;
        return this;
    }

    public Set<Node> getWorkers() {
        return workers;
    }

    public ClusterManager setWorkers(Set<Node> workers) {
        this.workers = workers;
        return this;
    }

    public ClusterManager addWorker(Node node) {
        this.workers.add(node);
        return this;
    }

    public ClusterManager removeWorker(Node node) {
        this.workers.remove(node);
        return this;
    }
}
