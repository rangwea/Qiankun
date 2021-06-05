package com.wikia.calabash.consistenthash;

public class VirtualConsistentNode<T extends ConsistentNode> implements ConsistentNode {
    final T physicalNode;
    final int replicaIndex;

    public VirtualConsistentNode(T physicalNode, int replicaIndex) {
        this.replicaIndex = replicaIndex;
        this.physicalNode = physicalNode;
    }

    @Override
    public String getKey() {
        return physicalNode.getKey() + "-" + replicaIndex;
    }

    public boolean isVirtualNodeOf(T pNode) {
        return physicalNode.getKey().equals(pNode.getKey());
    }

    public T getPhysicalNode() {
        return physicalNode;
    }
}