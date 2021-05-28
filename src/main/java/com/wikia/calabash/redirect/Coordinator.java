package com.wikia.calabash.redirect;

import org.springframework.stereotype.Service;

/**
 * @author wikia
 * @since 2020/1/8 17:32
 */
@Service
public class Coordinator {
    public boolean isMaster() {
        return false;
    }

    public String getMaster() {
        return "host:port";
    }
}
