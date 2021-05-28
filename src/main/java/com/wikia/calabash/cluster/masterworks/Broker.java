package com.wikia.calabash.cluster.masterworks;

import com.wikia.calabash.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetAddress;
import java.util.ArrayList;
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

    private MasterSelector masterSelector;
    private Node node;

    public Broker(BrokerConfig brokerConfig
            , ZkConfig zkConfig
            , List<Master> masters
    ) {
        this.brokerConfig = brokerConfig;
        this.zkConfig = zkConfig;
        this.masters = masters;
    }

    public void start() {
        try {
            log.info("broker starting:{}", brokerConfig);

            // 启动 zkClient
            CuratorFramework zkClient = CuratorFrameworkFactory.builder()
                    .connectString(zkConfig.getHost())
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                    .connectionTimeoutMs(zkConfig.getConnectionTimeout() == null ? 15 * 1000 : zkConfig.getConnectionTimeout()) //连接超时时间，默认15秒
                    .sessionTimeoutMs(zkConfig.getSessionTimeout() == null ? 60 * 1000 : zkConfig.getSessionTimeout()) //会话超时时间，默认60秒
                    .namespace(zkConfig.getNamespace() == null ? ZkPaths.DEFAULT_NAMESPACE : zkConfig.getNamespace()) //设置命名空间
                    .build();
            zkClient.start();

            String localHost = brokerConfig.getHost() == null ? InetAddress.getLocalHost().getHostAddress() : brokerConfig.getHost();
            int port = brokerConfig.getPort() == null ? DEFAULT_BROKER_PORT : brokerConfig.getPort();

            String nodeName = localHost + ":" + port;
            this.masterSelector = new MasterSelector(zkClient, nodeName, masters, new ArrayList<>());
            masterSelector.start();

            // 等待 3000 mill，如果还没有成为 master，自动退位为 worker
            Thread.sleep(3000);
            if (masterSelector.isLeader()) {
                log.info("current broker is leader:{}", brokerConfig);
            } else {
                // 注册 worker
                this.node = new Node(localHost, port);
                String nodeId = zkClient.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                        .forPath(ZkPaths.WORKER_ID_PATH_PREFIX, JacksonUtils.writeValueAsString(this.node).getBytes());
                this.node.setId(nodeId);
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

    public Node getNode() {
        return node;
    }
}
