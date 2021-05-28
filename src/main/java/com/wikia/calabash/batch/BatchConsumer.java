package com.wikia.calabash.batch;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
public class BatchConsumer {

    private volatile Queue<byte[]> elementQueue = new ConcurrentLinkedQueue<>();
    private ExecutorService executorService;

    private int maxBatchSize;
    private int maxBatchByteSize;
    private Consumer<List<byte[]>> consumer;
    private ExecuteListener<byte[]> executeListener;

    private int curBatchByteSize = 0;

    private BatchConsumer(int maxBatchSize, int maxBatchByteSize, int flushIntervalMill, Consumer<List<byte[]>> consumer, ExecutorService executorService, ExecuteListener<byte[]> executeListener) {
        this.maxBatchSize = maxBatchSize;
        this.maxBatchByteSize = maxBatchByteSize;
        this.executorService = executorService;
        this.consumer = consumer;
        this.executeListener = executeListener;

        Executors.newScheduledThreadPool(2)
                .scheduleAtFixedRate(this::execute, flushIntervalMill, flushIntervalMill, TimeUnit.MILLISECONDS);
    }

    public void call(byte[] t) {
        int byteSize = t.length;
        if (byteSize >= maxBatchByteSize) {
            log.warn("single message great than maxByteSize:maxByteSize={}", maxBatchByteSize);
            return;
        }
        sendIfNeed(byteSize);
        elementQueue.add(t);
    }

    private synchronized void sendIfNeed(int byteSize) {
        int nextSize = curBatchByteSize + byteSize;
        if (elementQueue.size() >= maxBatchSize || nextSize > maxBatchByteSize) {
            execute();
            curBatchByteSize = byteSize;
        } else {
            curBatchByteSize = nextSize;
        }
    }

    private void execute() {
        if (this.elementQueue == null || this.elementQueue.isEmpty()) {
            log.info("batch execute return: size={}", elementQueue.size());
            return;
        }
        log.info("batch execute: size={};bytes={}", elementQueue.size(), curBatchByteSize);

        Queue<byte[]> cur = this.elementQueue;
        this.elementQueue = new ConcurrentLinkedQueue<>();

        executorService.submit(() -> {
            List<byte[]> list = new ArrayList<>(cur);
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

    public static class Builder {
        private static final AtomicInteger count = new AtomicInteger();
        private String name;
        private int parallel;
        private int flushIntervalMill;
        private int maxBatchSize;
        private int maxBatchByteSize;
        private Consumer<List<byte[]>> consumer;
        private ExecutorService executorService;
        private ExecuteListener<byte[]> executeListener;

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

        public Builder maxBatchByteSize(int maxBatchByteSize) {
            this.maxBatchByteSize = maxBatchByteSize;
            return this;
        }

        public Builder consumer(Consumer<List<byte[]>> consumer) {
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

        public Builder executeListener(ExecuteListener<byte[]> executeListener) {
            this.executeListener = executeListener;
            return this;
        }

        public BatchConsumer build() {
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

            return new BatchConsumer(maxBatchSize, maxBatchByteSize, flushIntervalMill, consumer, executorService, executeListener);
        }
    }

    public static void main(String[] args) {
        BatchConsumer batchConsumer = BatchConsumer.builder()
                .maxBatchSize(10)
                .maxBatchByteSize(30)
                .consumer(System.out::println)
                .flushIntervalMill(100000)
                .build();

        for (int i = 0; i < 30; i++) {
            byte[] bytes = "12345678901234".getBytes();
            batchConsumer.call(bytes);
        }
    }
}
