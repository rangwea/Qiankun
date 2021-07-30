package com.wikia.calabash.executor;

import java.util.concurrent.FutureTask;

public class ComparableFutureTask<V> extends FutureTask<V> implements Runnable, Comparable<ComparableFutureTask<V>> {
    private final PriorityRunnable priorityRunnable;

    public ComparableFutureTask(Runnable priorityRunnable, V v) {
        super(priorityRunnable, v);
        this.priorityRunnable = (PriorityRunnable) priorityRunnable;
    }

    /**
     * priority 越小优先级越高
     */
    @Override
    public int compareTo(ComparableFutureTask<V> o) {
        return this.priorityRunnable.getPriority() - o.priorityRunnable.getPriority();
    }

}