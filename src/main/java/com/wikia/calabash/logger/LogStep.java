package com.wikia.calabash.logger;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Predicate;


@Slf4j
public class LogStep<T> {

    private Predicate<T> predicate;
    private String name;

    public LogStep(String name, Predicate<T> predicate) {
        this.name = name;
        this.predicate = predicate;
    }

    public LogStep<T> step(T t, String step, String format, Object... params) {
        return step(t, step, log, format, params);
    }

    public LogStep<T> step(T t, String step, Logger logger, String format, Object... params) {
        if (predicate.test(t)) {
            List<String> ps = Lists.newArrayList(name, step);
            if (params != null) {
                for (Object param : params) {
                    ps.add(param.toString());
                }
            }
            logger.info("[{}]-{}:" + format, ps.toArray());
        }
        return this;
    }

    public LogStep<T> step(T t, String step) {
        return step(t, step, log);
    }

    public LogStep<T> step(T t, String step, Logger logger) {
        if (predicate.test(t)) {
            logger.info("[{}]-{}", name, step);
        }
        return this;
    }
}
