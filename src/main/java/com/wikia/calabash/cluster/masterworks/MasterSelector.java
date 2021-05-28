package com.wikia.calabash.cluster.masterworks;

import com.wikia.calabash.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @author wikia
 * @since 5/21/2021 4:03 PM
 */
@Slf4j
public class MasterSelector extends LeaderSelectorListenerAdapter implements Closeable {
    private final String name;
    private final LeaderSelector leaderSelector;
    private final CuratorFramework client;
    private final CuratorCache workersCache;
    private final CountDownLatch closeLatch = new CountDownLatch(1);
    private final List<Master> masters;
    private final List<WorkersChangeListener> workersListeners;

    public MasterSelector(CuratorFramework client, String name, List<Master> masters, List<WorkersChangeListener> workersListeners) {
        this.client = client;
        this.name = name;
        this.masters = masters;
        this.workersListeners = workersListeners;
        this.workersCache = CuratorCache.build(client, "/workers");
        this.leaderSelector = new LeaderSelector(client, ZkPaths.MASTER_PATH, this);
        leaderSelector.autoRequeue();
    }

    public void start() {
        leaderSelector.start();
    }

    public boolean isLeader() {
        return this.leaderSelector.hasLeadership();
    }

    @Override
    public void close() {
        try {
            this.closeMasters();
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
            // 成为 Master, 缓存 workers
            workersCache.start();
            this.addWorksChangeListeners();

            this.startMasters();

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

    public List<Node> getWorkerInfos() {
        return this.workersCache.stream()
                .map(e -> JacksonUtils.readValue(e.getData(), Node.class))
                .collect(Collectors.toList());
    }

    private void startMasters() {
        for (Master masterListener : this.masters) {
            masterListener.start();
        }
    }

    private void closeMasters() {
        for (Master masterListener : this.masters) {
            masterListener.close();
        }
    }

    private void addWorksChangeListeners() {
        for (WorkersChangeListener workersListener : workersListeners) {
            this.workersCache.listenable()
                    .addListener((type, oldData, newData) -> {
                        switch (type) {
                            case NODE_CREATED:
                                workersListener.add(JacksonUtils.readValue(newData.getData(), Node.class));
                                break;
                            case NODE_DELETED:
                                workersListener.delete(JacksonUtils.readValue(oldData.getData(), Node.class));
                        }
                    });
        }
    }
}

