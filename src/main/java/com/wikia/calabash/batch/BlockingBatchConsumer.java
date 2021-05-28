package com.wikia.calabash.batch;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
public class BlockingBatchConsumer<T> {

    private LinkedBlockingQueue<T> elementQueue;
    private ExecutorService executorService;

    private int maxBatchSize;
    private Consumer<List<T>> consumer;
    private ExecuteListener<T> executeListener;

    private BlockingBatchConsumer(int maxBatchSize, int flushIntervalMill, Consumer<List<T>> consumer, ExecutorService executorService, ExecuteListener<T> executeListener) {
        this.maxBatchSize = maxBatchSize;
        this.executorService = executorService;
        this.consumer = consumer;
        this.executeListener = executeListener;
        this.elementQueue = new LinkedBlockingQueue<>(maxBatchSize);

        Executors.newScheduledThreadPool(2)
                .scheduleAtFixedRate(this::execute, flushIntervalMill, flushIntervalMill, TimeUnit.MILLISECONDS);
    }

    public void call(T t) throws InterruptedException {
        elementQueue.put(t);
    }

    private void execute() {
        if (this.elementQueue == null || this.elementQueue.isEmpty()) {
            log.info("batch execute empty");
            return;
        }
        log.info("batch execute: size={};", elementQueue.size());

        List<T> list = new ArrayList<>();
        this.elementQueue.drainTo(list);

        executorService.submit(() -> {
            try {
                consumer.accept(list);
                if (executeListener != null) {
                    executeListener.onSuccess(list);
                }
            } catch (Throwable e) {
                log.error("ScoreIncPersistent call fail:{}", e.getMessage());
                if (executeListener != null) {
                    executeListener.onFail(e, list);
                }
            }
        });
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder<T> {
        private static final AtomicInteger count = new AtomicInteger();
        private String name;
        private int parallel;
        private int flushIntervalMill;
        private int maxBatchSize;
        private Consumer<List<T>> consumer;
        private ExecutorService executorService;
        private ExecuteListener<T> executeListener;

        public Builder parallel(int parallel) {
            this.parallel = parallel;
            return this;
        }

        public Builder flushIntervalMill(int flushIntervalMill) {
            this.flushIntervalMill = flushIntervalMill;
            return this;
        }

        public Builder maxBatchSize(int maxBatchSize) {
            this.maxBatchSize = maxBatchSize;
            return this;
        }

        public Builder consumer(Consumer<List<T>> consumer) {
            this.consumer = consumer;
            return this;
        }

        public Builder executorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder executeListener(ExecuteListener<T> executeListener) {
            this.executeListener = executeListener;
            return this;
        }

        public BlockingBatchConsumer build() {
            this.name = this.name == null ? "Batch-Consumer-" + count.incrementAndGet() : this.name;
            this.parallel = this.parallel == 0 ? 5 : this.parallel;
            this.flushIntervalMill = this.flushIntervalMill == 0 ? 60000 : this.flushIntervalMill;
            this.maxBatchSize = this.maxBatchSize == 0 ? 100 : this.maxBatchSize;

            if (this.executorService == null) {
                this.executorService = Executors.newFixedThreadPool(this.parallel, r -> new Thread(r, this.name + "-Thread"));
            }

            if (consumer == null) {
                throw new IllegalArgumentException("consumer can't be null");
            }

            return new BlockingBatchConsumer(maxBatchSize, flushIntervalMill, consumer, executorService, executeListener);
        }
    }
}
