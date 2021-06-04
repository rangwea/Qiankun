package com.wikia.calabash.cluster.masterworks.transport;

import com.wikia.calabash.cluster.masterworks.ClusterManager;
import com.wikia.calabash.cluster.masterworks.Node;
import com.wikia.calabash.cluster.masterworks.RoundRobinLoadBalancer;
import com.wikia.calabash.reactor.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wikia
 * @since 5/31/2021 9:45 AM
 */
@Slf4j
public class HandlerClientProxy<T> implements Handler<T> {
    private RoundRobinLoadBalancer roundRobinLoadBalancer;
    private String handlerSimpleName;

    public HandlerClientProxy(ClusterManager clusterManager, String handlerSimpleName) {
        this.roundRobinLoadBalancer = new RoundRobinLoadBalancer(clusterManager);
        this.handlerSimpleName = handlerSimpleName;
    }

    @Override
    public void handle(T t) {
        Node server = roundRobinLoadBalancer.getServer();
        this.doHandler(server.getHost(), server.getPort(), t);
    }

    private String doHandler(String host, int port, T t) {
        String url = "http://" + host + ":" + port + "/handle/" + handlerSimpleName;
        HttpResponse<String> response = Unirest.post(url)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(t)
                .asString();
        String responseBody = response.getBody();
        log.info("send handle:url={};msg={};response={}", url, t, responseBody);
        return responseBody;
    }
}
