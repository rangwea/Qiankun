package com.wikia.calabash.reactor;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wikia
 * @since 5/19/2021 2:34 PM
 */
@Slf4j
public abstract class EventLoop {
    private static final AtomicInteger ID_SEQ = new AtomicInteger(1);

    private String id;
    private volatile boolean on;

    public EventLoop() {
        this(null);
    }

    public EventLoop(String id) {
        this.id = id;
        if (id == null) {
            this.id = this.getClass().getSimpleName() + "-" + ID_SEQ.getAndIncrement();
        }
    }

    public void start() {
        this.on = true;
        new Thread(EventLoop.this::run, this.id).start();
    }

    public void stop() {
        this.on = false;
    }

    public void run() {
        while (on) {
            try {
                doRun();
                Thread.sleep(ThreadLocalRandom.current().nextLong(1000, 3000));
            } catch (Exception e) {
                log.error("event loop run error", e);
            }
        }
    }

    public abstract void doRun();

}
