package com.wikia.calabash.cluster.masterworks;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;

/**
 * @author wikia
 * @since 6/5/2021 11:30 AM
 */
public abstract class BaseTaskListeningWorker {
    private String workerId;
    private CuratorFramework curator;

    private PathChildrenCache taskAssignChildrenCache;

    public BaseTaskListeningWorker(CuratorFramework curator, String workerId) {
        this.curator = curator;
        this.workerId = workerId;
    }

    public void start() {
        taskAssignChildrenCache = new PathChildrenCache(curator, ZkPaths.TASK_ASSIGN + "/" + workerId, true);
        taskAssignChildrenCache.getListenable().addListener((client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    addTask(event.getData().getData());
                    break;
                case CHILD_REMOVED:
                    removeTask(event.getData().getData());
                    break;
                case CHILD_UPDATED:
                    updateTask(event.getData().getData());
                    break;
            }
        });
    }

    protected abstract void addTask(byte[] taskDescription);

    protected abstract void removeTask(byte[] taskDescription);

    protected abstract void updateTask(byte[] taskDescription);
}
