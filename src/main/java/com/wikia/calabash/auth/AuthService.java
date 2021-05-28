package com.wikia.calabash.auth;

import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author wikia
 * @since 4/20/2021 11:32 AM
 */
@Component
@Slf4j
public class AuthService {
    @Resource
    private AuthClientDao authClientDao;
    @Resource
    private AuthClientSinkDao authClientSinkDao;

    /**
     * clientKey -> AuthClient
     */
    private Map<String, AuthClient> authClientMap = new HashMap<>();
    private Set<AuthClientSink> authClientApiRelationSet = new HashSet<>();

    @PostConstruct
    public void init() {
        flushCache();
        // 每10分钟刷新一次
        Executors.newScheduledThreadPool(2).schedule(this::flushCache, 10, TimeUnit.MINUTES);
    }

    public void authCheck(String clientKey, Long timestamp, String nonceStr, String sign, Long sinkId) {
        log.debug("auth check:clientKey={};timestamp={};nonceStr={};sign={};appId={}", clientKey, timestamp, nonceStr, sign, sinkId);

        // 没有传递 client 信息，auth 不通过
        if (clientKey == null || timestamp == null || nonceStr == null || sign == null || sinkId == null) {
            log.warn("auth reject [auth null]:clientKey={};timestamp={};nonceStr={};sign={};appId={}", clientKey, timestamp, nonceStr, sign, sinkId);
            throw new AuthException();
        }

        // 不存在的 client 信息，auth 不通过
        AuthClient authClient = authClientMap.get(clientKey);
        if (authClient == null) {
            log.warn("auth reject [client not existed]:clientKey={};", clientKey);
            throw new AuthException();
        }

        // 非法 client 信息，auth sign 不通过
        String serverSign = this.sign(clientKey, authClient.getClientSecret(), timestamp, nonceStr);
        if (!serverSign.equals(sign)) {
            log.warn("auth reject [sign invalid]:clientKey={};serverSign={};sign={};timestamp={};nonceStr={}", clientKey, serverSign, sign, timestamp, nonceStr);
            throw new AuthException();
        }

        // 没有访问当前 api 权限，auth 不通过
        AuthClientSink authClientApiRelation = new AuthClientSink(authClient.getId(), sinkId);
        if (!authClientApiRelationSet.contains(authClientApiRelation)) {
            log.warn("auth reject [client no auth]:clientKey={};apiId={}", clientKey, sinkId);
            throw new AuthException();
        }
    }

    private void flushCache() {
        this.authClientMap = authClientDao.getAll()
                .parallelStream()
                .collect(Collectors.toMap(AuthClient::getClientKey, e -> e));

        this.authClientApiRelationSet = new HashSet<>(authClientSinkDao.getAll());
    }

    private String sign(String clientKey, String clientSecret, Long timestamp, String nonceStr) {
        String s = clientKey + timestamp + nonceStr + clientSecret;
        return Hashing.md5().hashBytes(s.getBytes()).toString();
    }
}
