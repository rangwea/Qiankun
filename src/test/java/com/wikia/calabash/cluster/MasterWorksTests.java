package com.wikia.calabash.cluster;

import com.wikia.calabash.cluster.masterworks.Broker;
import com.wikia.calabash.cluster.masterworks.BrokerConfig;
import com.wikia.calabash.cluster.masterworks.Master;
import com.wikia.calabash.cluster.masterworks.ZkConfig;
import com.wikia.calabash.reactor.EventLoop;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wikia
 * @since 5/28/2021 3:18 PM
 */
public class MasterWorksTests {

    @Test
    public void test() throws IOException {
        BrokerConfig brokerConfig = new BrokerConfig();
        brokerConfig.setHost("127.0.0.1");
        brokerConfig.setPort(8091);

        ZkConfig zkConfig = new ZkConfig();
        zkConfig.setHost("127.0.0.1:2181");
        zkConfig.setNamespace("foo");

        List<Master> masters = new ArrayList<>();
        PingEventLoop pingEventLoop = new PingEventLoop(brokerConfig);
        PingMaster pingMaster = new PingMaster(pingEventLoop);
        masters.add(pingMaster);
        Broker broker = new Broker(brokerConfig, zkConfig, masters);
        broker.start();

        System.in.read();
    }


    public static class PingMaster implements Master {
        private PingEventLoop pingEventLoop;

        public PingMaster(PingEventLoop pingEventLoop) {
            this.pingEventLoop = pingEventLoop;
        }

        @Override
        public void start() {
            pingEventLoop.start();
        }

        @Override
        public void close() {
            pingEventLoop.stop();
        }
    }

    public static class PingEventLoop extends EventLoop {
        private BrokerConfig brokerConfig;

        public PingEventLoop(BrokerConfig brokerConfig) {
            this.brokerConfig = brokerConfig;
        }

        @Override
        public void doRun() {
            System.out.println(brokerConfig + " ping...");
        }

    }


}
