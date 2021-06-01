package com.wikia.calabash.logger;

import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 手动
 */
@RestController
@RequestMapping("/manual")
public class ManualController {
    @Resource
    private LoggingSystem loggingSystem;

    @GetMapping("/log/{loggerName}/{level}")
    public String changeLevel(@PathVariable String loggerName, @PathVariable LogLevel level) {
        loggingSystem.setLogLevel(loggerName, level);
        return "OK";
    }
}
