package com.wikia.calabash.executor;

/**
 * 利用 priority 字段来表示优先级
 */
public abstract class PriorityRunnable implements Runnable {
    private final int priority;

    public PriorityRunnable(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}