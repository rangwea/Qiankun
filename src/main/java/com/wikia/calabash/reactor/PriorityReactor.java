package com.wikia.calabash.reactor;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author feijianwu
 * @since 5/20/2021 2:56 PM
 */
@Slf4j
public class PriorityReactor<T> {
    private final Handler<T> handler;
    private final PriorityThreadPoolExecutor executor;

    public PriorityReactor(Handler<T> handler, int queueSize, int corePoolSize, int maximumPoolSize) {
        this.handler = handler;
        // 使用优先级队列，任务具有优先级
        PriorityBlockingQueue<Runnable> blockingQueue = new PriorityBlockingQueue<>(queueSize);
        // 拒绝策略：如果队列已满，阻塞提交线程
        RejectedExecutionHandler rejectedExecutionHandler = (r, executor) -> {
            if (!executor.isShutdown()) {
                try {
                    // 队列满，阻塞
                    executor.getQueue().put(r);
                } catch (Exception e) {
                    log.error("PriorityReactor put task exception", e);
                }
            }
        };
        this.executor = new PriorityThreadPoolExecutor(corePoolSize, maximumPoolSize, 5, TimeUnit.SECONDS, blockingQueue, r -> new Thread(r, "Priority-Reactor-Thread"), rejectedExecutionHandler);
    }

    /**
     * @param t        处理 msg
     * @param priority 优先级，数值越大优先级越高
     */
    public void handle(T t, int priority) {
        this.executor.submit(new PriorityRunnable(priority) {
            @Override
            public void run() {
                handler.handle(t);
            }
        });
    }

    public void close() {
        this.executor.shutdown();
    }

    static class PriorityThreadPoolExecutor extends ThreadPoolExecutor {

        public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }

        @Override
        protected <V> RunnableFuture<V> newTaskFor(Runnable runnable, V value) {
            return new ComparableFutureTask<>(runnable, value);
        }
    }

    static abstract class PriorityRunnable implements Runnable {
        private final int priority;

        PriorityRunnable(int priority) {
            this.priority = priority;
        }
    }

    static class ComparableFutureTask<V> extends FutureTask<V> implements Runnable, Comparable<ComparableFutureTask<V>> {
        private final PriorityRunnable priorityRunnable;

        public ComparableFutureTask(Runnable priorityRunnable, V v) {
            super(priorityRunnable, v);
            this.priorityRunnable = (PriorityRunnable) priorityRunnable;
        }

        /**
         * priority 越大优先级越高
         */
        @Override
        public int compareTo(ComparableFutureTask<V> o) {
            return o.priorityRunnable.priority - this.priorityRunnable.priority;
        }

    }
}
