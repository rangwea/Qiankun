package com.wikia.calabash.cluster.masterworks;

import com.wikia.calabash.consistenthash.ConsistentHashRouter;
import com.wikia.calabash.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wikia
 * @since 6/5/2021 2:24 PM
 */
@Slf4j
public abstract class BaseTaskAssignMaster implements Master {
    private CuratorFramework curator;
    private ClusterManager clusterManager;
    private int consistentHashVirtualNodeNum;

    private PathChildrenCache assignChildrenCache;
    private ConsistentHashRouter<Node> consistentHashRouter;

    private Map<String, Node> nodeByTaskKey;

    public BaseTaskAssignMaster(CuratorFramework curator, ClusterManager clusterManager, int consistentHashVirtualNodeNum) {
        this.curator = curator;
        this.clusterManager = clusterManager;
        this.consistentHashVirtualNodeNum = consistentHashVirtualNodeNum;
    }

    @Override
    public void start() {
        this.assignChildrenCache = new PathChildrenCache(curator, ZkPaths.TASK_ASSIGN, true);

        Set<Node> workers = clusterManager.getWorkers();
        this.consistentHashRouter = new ConsistentHashRouter<>(new ArrayList<>(workers), consistentHashVirtualNodeNum);

        this.nodeByTaskKey = new HashMap<>();

        try {
            this.shuffle();
        } catch (Exception e) {
            log.error("master start fail:{}", this.getClass());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            this.assignChildrenCache.close();
            this.consistentHashRouter = null;
            this.nodeByTaskKey = null;
        } catch (Exception e) {
            log.error("master stop fail:{}", this.getClass());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void workersChange(PathChildrenCacheEvent event) {
        try {
            Node node = JacksonUtils.readValue(event.getData().getData(), Node.class);

            PathChildrenCacheEvent.Type type = event.getType();
            switch (type) {
                case CHILD_ADDED:
                    consistentHashRouter.addNode(node, consistentHashVirtualNodeNum);
                    shuffle();
                    break;
                case CHILD_REMOVED:
                    consistentHashRouter.removeNode(node);
                    break;
            }
        } catch (Exception e) {
            log.info("workers change process fail:event={}", event, e);
        }
    }

    private void shuffle() throws Exception {
        List<Task> allTask = this.allTasks();
        for (Task task : allTask) {
            reassignTaskIfNeed(task);
        }
    }

    public void addTask(Task task) throws Exception {
        Node node = consistentHashRouter.routeNode(task.getKey());
        addTaskAssignment(task, node);
        this.nodeByTaskKey.put(task.getKey(), node);
    }

    public void removeTask(String taskKey) throws Exception {
        Node node = nodeByTaskKey.get(taskKey);
        removeTaskAssignment(taskKey, node);
        this.nodeByTaskKey.remove(taskKey);
    }

    public void updateTask(Task task) throws Exception {
        Node node = this.nodeByTaskKey.get(task.getKey());
        this.updateTaskAssignment(task, node);
    }

    public void reassignTaskIfNeed(Task task) throws Exception {
        String taskKey = task.getKey();
        Node oldNode = nodeByTaskKey.get(taskKey);
        Node newNode = consistentHashRouter.routeNode(taskKey);
        if (!newNode.equals(oldNode)) {
            reassignTaskAssignment(task, oldNode, newNode);
            this.nodeByTaskKey.put(taskKey, newNode);
        }
    }

    private void reassignTaskAssignment(Task task, Node oldNode, Node newNode) throws Exception {
        this.removeTaskAssignment(task.getKey(), oldNode);
        this.addTaskAssignment(task, newNode);
    }

    private void addTaskAssignment(Task task, Node node) throws Exception {
        curator.create()
                .withMode(CreateMode.PERSISTENT)
                .forPath(getTaskAssignPath(task.getKey(), node), JacksonUtils.writeValueAsString(task).getBytes());
    }

    private void removeTaskAssignment(String taskKey, Node node) throws Exception {
        curator.delete()
                .forPath(ZkPaths.TASK_ASSIGN + node.getKey() + "/" + taskKey);
    }

    private void updateTaskAssignment(Task task, Node node) throws Exception {
        curator.setData()
                .forPath(getTaskAssignPath(task.getKey(), node), JacksonUtils.writeValueAsString(task).getBytes());
    }

    private String getTaskAssignPath(String taskKey, Node node) {
        return ZkPaths.TASK_ASSIGN + node.getKey() + "/" + taskKey;
    }

    protected abstract List<Task> allTasks();
}
