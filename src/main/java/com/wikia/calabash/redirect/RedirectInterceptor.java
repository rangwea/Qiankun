package com.wikia.calabash.redirect;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Optional;

/**
 * @author wikia
 * @since 2020/1/2 20:58
 */
@Component
@Slf4j
public class RedirectInterceptor implements HandlerInterceptor {

    @Resource
    private Coordinator coordinator;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        if (Excludes.paths.contains(requestURI)) {
            return true;
        }
        if (!needMaster(request)) {
            log.info("不需要使用Master执行:{}", requestURI);
            return true;
        }

        if (coordinator.isMaster()) {
            return true;
        }

        String master = coordinator.getMaster();
        if (coordinator.isMaster()) {
            log.info("当前服务是Master，直接执行:{}", requestURI);
            return true;
        }

        String redirectUrl = "http://" + master + requestURI;
        String queryString = request.getQueryString();
        if (!Strings.isNullOrEmpty(queryString)) {
            redirectUrl = redirectUrl + "?" + queryString;
        }
        HttpMethod httpMethod = Optional.ofNullable(HttpMethod.resolve(request.getMethod())).orElse(HttpMethod.GET);
        InputStream inputStream = request.getInputStream();
        HttpEntity<String> httpEntity = new HttpEntity<>(new String(ByteStreams.toByteArray(inputStream)));

        log.info("请求Master节点开始:redirectUrl={};httpEntity={}", redirectUrl, httpEntity);
        ResponseEntity<String> responseEntity = restTemplate.exchange(redirectUrl, httpMethod, httpEntity, String.class);
        log.info("请求Master节点完成:redirectUrl={}, response={}", redirectUrl, responseEntity);

        response.setStatus(responseEntity.getStatusCode().value());
        HttpHeaders headers = responseEntity.getHeaders();
        headers.forEach((k, v) -> v.forEach(e -> response.setHeader(k, e)));
        PrintWriter writer = response.getWriter();
        writer.write(Optional.ofNullable(responseEntity.getBody()).orElse(""));

        return false;
    }

    private boolean needMaster(HttpServletRequest request) {
        String needMaster = request.getHeader("Need-Master");
        return "1".equals(needMaster);
    }

}
