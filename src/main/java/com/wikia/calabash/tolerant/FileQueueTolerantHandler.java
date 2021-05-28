package com.wikia.calabash.tolerant;

import com.wikia.calabash.queue.RichBDBQueueV2;
import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author wikia
 * @since 6/29/2020 9:05 PM
 */
public class FileQueueTolerantHandler<T> implements TolerantHandler {
    private RichBDBQueueV2<FailRecord<T>> queue;

    public FileQueueTolerantHandler(String name, Consumer<T> consumer, Class<T> clz) {
        try {
            File file = Paths.get("D://", name).toFile();
            this.queue = new RichBDBQueueV2<>(file, name, 10, tf -> tf.constructParametricType(FailRecord.class, clz));
            startConsumer(consumer);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public void failSave(T t) {
        FailRecord<T> failRecord = new FailRecord<>(t);
        doFailSave(failRecord);
    }

    private void doFailSave(FailRecord<T> record) {
        try {
            record.counterInc();
            queue.push(record);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private void startConsumer(Consumer<T> consumer) {
        ConsumerMaster<T> queueConsumer = new ConsumerMaster<>(2, consumer, this::doFailSave);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                this.queue.consume(queueConsumer);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    @Override
    public void handle(ProceedingJoinPoint pjp, CatchTolerant catchTolerant) {

    }

    @Data
    public static class FailRecord<T> {
        private int counter;
        private T data;

        public FailRecord() {

        }

        public FailRecord(T data) {
            this.data = data;
            this.counter = 1;
        }

        public void counterInc() {
            this.counter++;
        }
    }

    public static class ConsumerMaster<T> implements com.wikia.calabash.queue.Consumer<FailRecord<T>> {
        private Consumer<FailRecord<T>> failRecordConsumer;
        private Consumer<T> consumer;
        private ExecutorService executorService;

        public ConsumerMaster(int works, Consumer<T> consumer, Consumer<FailRecord<T>> failRecordConsumer) {
            this.consumer = consumer;
            this.executorService = Executors.newFixedThreadPool(works);
            this.failRecordConsumer = failRecordConsumer;
        }

        @Override
        public boolean consume(FailRecord<T> item) {
            LinkedBlockingQueue<Object> objects = new LinkedBlockingQueue<>(1);

            executorService.submit(() -> {
                try {
                    consumer.accept(item.data);
                } catch (Throwable throwable) {
                    failRecordConsumer.accept(item);
                }
            });
            return true;
        }

    }

    public static void main(String[] args) {
        FileQueueTolerantHandler<Record> tolerantHandler = new FileQueueTolerantHandler<>("test", record -> {
            try {
                Thread.sleep(100);
                System.out.println(record);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Record.class);

        AtomicInteger counter = new AtomicInteger();
        ExecutorService producer = Executors.newFixedThreadPool(10);
        producer.submit(() -> {
            while (true) {
                Record record = new Record();
                record.setId(counter.incrementAndGet());
                record.setMessage("test");
                record.setTimestamp(System.currentTimeMillis());
                tolerantHandler.failSave(record);
                Thread.sleep(100);
            }
        });
    }

    @Data
    public static class Record {
        private Integer id;
        private String message;
        private long timestamp;
    }
}
