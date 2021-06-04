package com.wikia.calabash.cluster.masterworks.transport;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wikia
 * @since 5/31/2021 2:33 PM
 */
@RestController
@RequestMapping("/handle")
public class HandlerServerProxy {

    @PostMapping(value = "/handle1", produces = "application/json")
    public int handle() {
        return 1;
    }

}
