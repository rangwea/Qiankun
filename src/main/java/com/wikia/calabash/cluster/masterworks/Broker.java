package com.wikia.calabash.cluster.masterworks;

import com.google.common.base.Preconditions;
import com.wikia.calabash.clean.Defaults;
import com.wikia.calabash.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetAddress;
import java.util.List;

/**
 * @author wikia
 * @since 5/22/2021 10:23 AM
 */
@Slf4j
public class Broker {
    private static final int DEFAULT_BROKER_PORT = 9023;

    private final BrokerConfig brokerConfig;
    private final ZkConfig zkConfig;
    private final List<Master> masters;
    private ClusterManager clusterManager;
    private MasterSelector masterSelector;

    public Broker(BrokerConfig brokerConfig
            , ZkConfig zkConfig
            , List<Master> masters
            , ClusterManager clusterManager
    ) {
        this.brokerConfig = brokerConfig;
        this.zkConfig = zkConfig;
        this.masters = masters;
        this.clusterManager = clusterManager;
    }

    public void start() {
        try {
            log.info("broker starting:{}", brokerConfig);
            Preconditions.checkNotNull(brokerConfig);
            Preconditions.checkNotNull(brokerConfig.getPort());
            Preconditions.checkNotNull(zkConfig);
            Preconditions.checkNotNull(zkConfig.getConnection());
            Preconditions.checkNotNull(clusterManager);

            // 启动 zkClient
            CuratorFramework zkClient = CuratorFrameworkFactory.builder()
                    .connectString(zkConfig.getConnection())
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                    .connectionTimeoutMs(zkConfig.getConnectionTimeout() == null ? 15 * 1000 : zkConfig.getConnectionTimeout()) //连接超时时间，默认15秒
                    .sessionTimeoutMs(zkConfig.getSessionTimeout() == null ? 60 * 1000 : zkConfig.getSessionTimeout()) //会话超时时间，默认60秒
                    .namespace(zkConfig.getNamespace() == null ? ZkPaths.DEFAULT_NAMESPACE : zkConfig.getNamespace()) //设置命名空间
                    .build();
            zkClient.start();

            String localHost = brokerConfig.getHost() == null ? InetAddress.getLocalHost().getHostAddress() : brokerConfig.getHost();
            int port = brokerConfig.getPort() == null ? DEFAULT_BROKER_PORT : brokerConfig.getPort();

            Node node = new Node(localHost, port);
            clusterManager.setLocalNode(node);

            String nodeName = localHost + ":" + port;
            masterSelector = new MasterSelector(zkClient, nodeName, masters, clusterManager);
            masterSelector.start();

            // 等待 3000 mill
            Thread.sleep(3000);
            Boolean allowMasterDoWork = Defaults.ifNull(brokerConfig.getAllowMasterDoWork(), true);
            if (masterSelector.isLeader() && !allowMasterDoWork) {
                log.info("current broker is leader:{}", brokerConfig);
                clusterManager.setLocalIsLeader(true);
            } else {
                // 如果未被选举成为 Master，或者允许 Master 也承担 worker 工作，那么注册 worker
                zkClient.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                        .forPath(ZkPaths.WORKER_ID_PATH_PREFIX, JacksonUtils.writeValueAsString(node).getBytes());

                clusterManager.setLocalIsLeader(false);
                log.info("register worker:{}", node);
            }

            log.info("broker started:{}", brokerConfig);
        } catch (Exception e) {
            log.error("register broker fail", e);
            throw new RuntimeException("register broker fail", e);
        }
    }

    public void stop() {
        this.masterSelector.close();
    }
}
