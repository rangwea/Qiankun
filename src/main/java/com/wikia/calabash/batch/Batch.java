package com.wikia.calabash.batch;

import com.google.common.collect.Lists;
import com.wikia.calabash.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


@Slf4j
public class Batch {
    private static ForkJoinPool forkJoinPool = new ForkJoinPool(16);

    public static <T> void invoke(List<T> list, Consumer<List<T>> consumer, boolean... async) {
        if (CollectionUtil.isNullOrEmpty(list)) {
            log.warn("invoke list is empty:{}", list);
            return;
        }
        try {
            ForkJoinTask<?> forkJoinTask = forkJoinPool.submit(() -> Lists.partition(list, 100)
                    .parallelStream()
                    .forEach(l -> {
                        try {
                            consumer.accept(l);
                        } catch (Exception e) {
                            log.error("batch invoke fail:{}", l.get(0), e);
                        }
                    }));

            if (async == null || !async[0]) {
                forkJoinTask.get();
            }
        } catch (Exception e) {
            log.error("batch invoke fail:{}", list.get(0), e);
        }
    }

    public static <T, U> void invoke(List<T> list, U param, BiConsumer<List<T>, U> consumer, boolean... async) {
        if (CollectionUtil.isNullOrEmpty(list)) {
            log.warn("invoke list is empty:{}", list);
            return;
        }
        try {
            ForkJoinTask<?> forkJoinTask = forkJoinPool.submit(() -> Lists.partition(list, 100)
                    .parallelStream()
                    .forEach(l -> {
                        try {
                            consumer.accept(l, param);
                        } catch (Exception e) {
                            log.error("batch invoke fail:{}", l.get(0), e);
                        }
                    }));

            if (async == null || !async[0]) {
                forkJoinTask.get();
            }
        } catch (Exception e) {
            log.error("batch invoke fail:{}", list.get(0), e);
        }
    }
}
