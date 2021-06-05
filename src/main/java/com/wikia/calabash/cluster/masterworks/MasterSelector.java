package com.wikia.calabash.cluster.masterworks;

import com.wikia.calabash.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author wikia
 * @since 5/21/2021 4:03 PM
 */
@Slf4j
public class MasterSelector extends LeaderSelectorListenerAdapter implements Closeable {
    private final String name;
    private final LeaderSelector leaderSelector;
    private final CuratorFramework client;
    private final PathChildrenCache workersCache;
    private final CountDownLatch closeLatch = new CountDownLatch(1);
    private final List<Master> masters;
    private final ClusterManager clusterManager;

    public MasterSelector(CuratorFramework client, String name, List<Master> masters, ClusterManager clusterManager) {
        this.client = client;
        this.name = name;
        this.masters = masters;
        this.clusterManager = clusterManager;
        this.workersCache = new PathChildrenCache(client, ZkPaths.WORKERS_PATH, true);
        this.leaderSelector = new LeaderSelector(client, ZkPaths.MASTER_PATH, this);
        leaderSelector.autoRequeue();
    }

    public void start() {
        leaderSelector.start();
        log.info("Start Master Selector");
    }

    public boolean isLeader() {
        return this.leaderSelector.hasLeadership();
    }

    @Override
    public void close() {
        try {
            this.stopMasters();
            closeLatch.countDown();
            leaderSelector.close();
            workersCache.close();
            client.close();
            log.info("Close Master Selector");
        } catch (Exception e) {
            log.warn("close fail", e);
        }
    }

    @Override
    public void takeLeadership(CuratorFramework client) {
        log.info("{} is now the leader.", this.name);
        try {
            // 成为 Master, 缓存 workers
            workersCache.start();
            // 监听 workers 变化
            workersCache.getListenable().addListener(this.workersCacheListener);

            // 启动所有 master 任务
            this.startMasters();

            /*
             * This latch is to prevent this call from exiting. If we exit, then we release mastership.
             */
            closeLatch.await();
        } catch (Exception e) {
            log.warn("{} take leader ship fail.", name);
        } finally {
            log.info("{} relinquishing leadership.", name);
        }
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        switch (newState) {
            case CONNECTED:
                //Nothing to do in this case.
                break;
            case RECONNECTED:
                // Reconnected, so I should still be the leader.
                break;
            case SUSPENDED:
                log.warn("Session suspended");
                break;
            case LOST:
                close();
                break;
            case READ_ONLY:
                // We ignore this case
                break;
        }
    }

    PathChildrenCacheListener workersCacheListener = new PathChildrenCacheListener() {
        @Override
        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) {
            ChildData data = event.getData();
            PathChildrenCacheEvent.Type type = event.getType();
            switch (type) {
                case CHILD_ADDED:
                    log.info("join a worker:{}", new String(data.getData()));
                    clusterManager.addWorker(JacksonUtils.readValue(data.getData(), Node.class));
                    break;
                case CHILD_REMOVED:
                    log.info("leave a worker:{}", new String(data.getData()));
                    clusterManager.addWorker(JacksonUtils.readValue(data.getData(), Node.class));
                    break;
            }

            // 通知所有 Master 实例
            masters.forEach(master -> {
                if (master.isStart()) {
                    master.workersChange(event);
                }
            });
        }
    };

    private void startMasters() {
        while (clusterManager.getWorkers().isEmpty()) {
            try {
                log.info("Workers not ready, master waiting to start...");
                Thread.sleep(200);
            } catch (Exception e) {
                log.error("thread exception", e);
            }
        }

        log.info("Has ready workers, masters is starting...");
        for (Master master : this.masters) {
            master.start();
        }
    }

    private void stopMasters() {
        log.info("Starting stop all masters...");
        for (Master master : this.masters) {
            master.stop();
        }
    }
}

