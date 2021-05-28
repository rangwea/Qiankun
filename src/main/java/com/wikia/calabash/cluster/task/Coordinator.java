package com.wikia.calabash.cluster.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;

import java.io.Closeable;
import java.util.concurrent.CountDownLatch;

/**
 * @author wikia
 * @since 5/21/2021 4:03 PM
 */
@Slf4j
public class Coordinator extends LeaderSelectorListenerAdapter implements Closeable {
    private final String name;
    private final LeaderSelector leaderSelector;
    private final CuratorFramework client;

    private final CuratorCache workersCache;
    private final CuratorCache tasksCache;

    private final CountDownLatch closeLatch = new CountDownLatch(1);

    public Coordinator(CuratorFramework client, String name) {
        this.client = client;
        this.name = name;
        this.leaderSelector = new LeaderSelector(client, "/master", this);
        this.workersCache = CuratorCache.build(client, "/workers");
        this.tasksCache = CuratorCache.build(client, "/tasks");
        leaderSelector.autoRequeue();
    }

    public void start() throws Exception {
        leaderSelector.start();

        client.create().forPath("/workers");
        client.create().forPath("/tasks");
        client.create().forPath("/assign");
        client.create().forPath("/status");
    }

    public boolean isLeader() {
        return this.leaderSelector.hasLeadership();
    }

    @Override
    public void close() {
        try {
            closeLatch.countDown();
            leaderSelector.close();
            workersCache.close();
            if (!client.getState().equals(CuratorFrameworkState.STOPPED)) {
                client.close();
            }
        } catch (Exception e) {
            log.warn("close fail", e);
        }
    }

    @Override
    public void takeLeadership(CuratorFramework client) {
        log.info("{} is now the leader.", this.name);
        try {
            // 监听缓存 workers
            workersCache.start();
            workersCache.listenable().addListener(worksCacheListener);
            // 监听缓存 tasks
            tasksCache.start();



            /*
             * This latch is to prevent this call from exiting. If we exit, then we release mastership.
             */
            closeLatch.await();
        } catch (InterruptedException e) {
            log.warn("{} was interrupted.", name);
        } finally {
            log.info("{} relinquishing leadership.", name);
        }
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        switch (newState) {
            case SUSPENDED:
                log.warn("Session suspended");
                break;
            case LOST:
                close();
                break;
        }
    }

    private void assignTasks(ChildData newWorker) {

    }

    private void reAssignTasks(ChildData leavedWorker) {

    }

    CuratorCacheListener worksCacheListener = new CuratorCacheListener() {
        @Override
        public void event(CuratorCacheListener.Type type, ChildData oldData, ChildData data) {
            switch (type) {
                case NODE_CREATED:
                    assignTasks(data);
                    break;
                case NODE_DELETED:
                    reAssignTasks(oldData);
                    break;
            }
        }
    };



}

