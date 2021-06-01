package com.wikia.calabash.logger;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;


@Slf4j
public class ThreadContextLogStep {
    private LogStep<String> logStep;
    private ThreadLocal<String> markThreadLocal = new ThreadLocal<>();

    public ThreadContextLogStep(String name, String match) {
        Predicate<String> threadContextPredicate = match1 -> {
            if (match1 == null || match1.isEmpty()) {
                return false;
            }
            return match1.equals(markThreadLocal.get());
        };
        this.logStep = new LogStep<>(name, threadContextPredicate);
    }

    public ThreadContextLogStep start(String mark) {
        markThreadLocal.set(mark);
        return this;
    }

    public ThreadContextLogStep stop() {
        markThreadLocal.remove();
        return this;
    }


}
