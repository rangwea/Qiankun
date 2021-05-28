package com.wikia.calabash.cluster.masterworks;

import lombok.Data;

/**
 * @author wikia
 * @since 5/22/2021 11:27 AM
 */
@Data
public class ZkConfig {
    private String host;
    private Integer connectionTimeout;
    private Integer sessionTimeout;
    private String namespace;
}
