package com.wikia.calabash.batch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.squareup.tape2.QueueFile;
import com.wikia.calabash.jackson.JacksonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * 本地磁盘重试队列， 通过 {@link #add(T)} 方法添加失败的 Record ，此类会将 Record 存入本地磁盘队列 {@link #sickQueue}，
 * 后台线程会消费队列进行重试，如果重试失败，整个重试机制进入静默期，在静默 {@link #silentMills} 时间后重新
 * 开始消费重试，如果一个 Record 重试次数大于 {@link #maxRetryTime} 次数，将被放入 {@link #deadQueue} 队列，从此不再被消费。
 * 用户可以通过 {@link #dumpDeadQueue()} 方法 dump 所有的 dead records ，
 * 并进行旁路处理，dump 的数据在 {@link #path} 目录的 "dead.dump" 文件中。
 * </p>
 *
 * <strong>Note: 如果使用此类进行重试，那么将不能保证数据的有序性</strong>
 *
 * @author wikia
 * @since 1/13/2021 5:27 PM
 */
@Slf4j
public class DiskRetryManager<T> {
    private final String path;
    private final QueueFile sickQueue;
    private final QueueFile deadQueue;
    private final DiskRetryConsumer<T> consumer;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private volatile boolean running = false;

    private long silentMills;
    private int maxRetryTime;

    public DiskRetryManager(String path, long silentMills, int maxRetryTime, DiskRetryConsumer<T> consumer) {
        try {
            File filePath = new File(path);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            this.path = path;
            this.sickQueue = new QueueFile.Builder(new File(path + "/sick"))
                    .build();
            this.deadQueue = new QueueFile.Builder(new File(path + "/dead"))
                    .build();

            this.silentMills = silentMills;
            this.maxRetryTime = maxRetryTime;
            this.consumer = consumer;
            this.start();
            log.info("start a DiskRetryManager:file={};silentMills={};maxRetryTime={}", path, silentMills, maxRetryTime);
        } catch (Exception e) {
            throw new RuntimeException("build DiskRetryManager fail", e);
        }
    }

    public void add(T record) {
        if (!this.running) {
            return;
        }
        try {
            RetryRecord<T> retryRecord = new RetryRecord<>();
            retryRecord.setTimestamp(System.currentTimeMillis());
            retryRecord.setRetryTime(1);
            retryRecord.setData(record);
            this.add(retryRecord);
        } catch (Exception e) {
            log.error("add record into disk queue fail:record={}", record, e);
        }
    }

    private void add(RetryRecord<T> retryRecord) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            byte[] bytes = this.serialize(retryRecord);
            sickQueue.add(bytes);
            notEmpty.signal();
            log.debug("queue file add element:{}", retryRecord);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public void start() {
        if (running) {
            return;
        }
        log.info("DiskRetryManager starting:silent={}", silentMills);
        new Thread(() -> {
            while (running) {
                final RetryRecord<T> record;
                try {
                    record = take();
                    consumer.retry(record.getData(), () -> failProcess(record));
                    log.info("disk retry process record:record={}", record);
                } catch (InterruptedException e) {
                    log.warn("InterruptedException", e);
                }
            }
        }).start();
        // 状态设置为运行中
        running = true;
        log.info("DiskRetryManager started");
    }

    public void stop() {
        try {
            this.running = false;
            sickQueue.close();
            log.info("DiskRetryManager stopped");
        } catch (Exception e) {
            log.error("stop DiskRetryManager fail", e);
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    public RetryRecord<T> take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (sickQueue.isEmpty()) {
                notEmpty.await();
            }
            byte[] bytes = sickQueue.peek();
            sickQueue.remove();
            RetryRecord<T> record = this.deserialize(bytes);
            log.debug("take a element:record={}", record);
            return record;
        } catch (IOException ioe) {
            log.error("peek queue fail", ioe);
            return null;
        } finally {
            lock.unlock();
        }
    }

    public void dumpDeadQueue() {
        try {
            File file = Paths.get(this.path, "dead.dump").toFile();
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            Iterator<byte[]> iterator = this.deadQueue.iterator();
            int size = 0;
            while (iterator.hasNext()) {
                byte[] next = iterator.next();
                RetryRecord<T> record = this.deserialize(next);
                String data = JacksonUtils.writeValueAsString(record.getData()) + "\n";
                Files.write(file.toPath(), data.getBytes(), StandardOpenOption.APPEND);
                size++;
            }
            log.info("dump dead queue:recordSize={}", size);
        } catch (Exception e) {
            log.error("dump dead queue fail", e);
        }
    }

    public void setSilentMills(long silentMills) {
        this.silentMills = silentMills;
    }

    public void setMaxRetryTime(int maxRetryTime) {
        this.maxRetryTime = maxRetryTime;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    private void failProcess(RetryRecord<T> record) {
        if (record != null) {
            if (record.getRetryTime() >= maxRetryTime) {
                // 大于最大重试次数，放入死亡队列，由人工排查，并做对应操作
                toDead(record);
            } else {
                // 重新放回重试队列末尾
                record.retryIncrement();
                add(record);
            }
        }
        // 重试失败，进入静默期，等待下一次重启
        silent();
    }

    private void silent() {
        try {
            log.info("enter into silent age.");
            Thread.sleep(silentMills);
        } catch (Exception e) {
            log.error("silent fail", e);
        }
    }

    private void toDead(RetryRecord<T> record) {
        try {
            log.warn("add a record to dead:record={}", record);
            byte[] bytes = this.serialize(record);
            this.deadQueue.add(bytes);
        } catch (Exception e) {
            log.error("add to dead queue fail:record={}", record);
        }
    }

    private byte[] serialize(RetryRecord<T> record) {
        return JacksonUtils.writeValueAsString(record).getBytes();
    }

    private RetryRecord<T> deserialize(byte[] bytes) {
        return JacksonUtils.readValue(bytes, new TypeReference<RetryRecord<T>>() {
        });
    }

}
