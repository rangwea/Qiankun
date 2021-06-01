package com.wikia.calabash.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据配置和 MDC 上下文过滤 DEBUG 日志
 */
public class ContextDebugFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        // 如果不是 DEBUG , 则放过
        if (!Level.DEBUG.equals(event.getLevel())) {
            return FilterReply.NEUTRAL;
        }

        ConcurrentHashMap<String, String> filterKeyValues = ContextDebugConfig.getFilterKeyValues();
        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
        if (!filterKeyValues.isEmpty() && !mdcPropertyMap.isEmpty()) {
            for (Map.Entry<String, String> keyValue : filterKeyValues.entrySet()) {
                String key = keyValue.getKey();
                String value = keyValue.getValue();
                if (value.equals(mdcPropertyMap.get(key))) {
                    // 上下文和配置匹配，则打印日志
                    return FilterReply.ACCEPT;
                }
            }
        }

        // 没有和配置匹配的上下文，不打印日志
        return FilterReply.DENY;
    }

}