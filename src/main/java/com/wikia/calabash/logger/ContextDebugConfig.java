package com.wikia.calabash.logger;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wufj(wikia_wu @ kingdee.com)
 * @since 4/8/2021 3:48 PM
 */
@Slf4j
@Configuration
public class ContextDebugConfig {
    private static final String CONFIG_PREFIX = "context.debug";

    private static ConcurrentHashMap<String, String> filterKeyValues = new ConcurrentHashMap<>();

    @ApolloConfig
    private Config config;

    @PostConstruct
    public void init() {
        Set<String> propertyNames = config.getPropertyNames();
        for (String propertyName : propertyNames) {
            if (StringUtils.startsWithIgnoreCase(propertyName, CONFIG_PREFIX)) {
                String key = propertyName.substring(CONFIG_PREFIX.length() + 1);
                String value = config.getProperty(CONFIG_PREFIX + "." + key, null);
                if (value != null) {
                    filterKeyValues.put(key, value);
                }
            }
        }
        log.info("init context logger filterKeyValues:{}", filterKeyValues);
    }

    @ApolloConfigChangeListener(interestedKeyPrefixes = CONFIG_PREFIX)
    private void changeFilterKeyValues(ConfigChangeEvent changeEvent) {
        log.info("onchange filterKeyValues");
        Set<String> changedKeys = changeEvent.changedKeys();
        for (String changedKey : changedKeys) {
            ConfigChange change = changeEvent.getChange(changedKey);
            switch (change.getChangeType()) {
                case ADDED:
                case MODIFIED:
                    filterKeyValues.put(changedKey, change.getNewValue());
                    break;
                case DELETED:
                    filterKeyValues.remove(changedKey);
                    break;
            }
            log.info("change filter key value:key={};oldValue={};newValue={};type={}", changedKey, change.getOldValue(), change.getNewValue(), change.getChangeType());
        }
        log.info("onchange current filterKeyValues:{}", filterKeyValues);
    }

    public static ConcurrentHashMap<String, String> getFilterKeyValues() {
        return filterKeyValues;
    }
}
