package com.wikia.calabash.exception;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class Exceptions {
    @FunctionalInterface
    public interface CheckedOperation {
        void execute() throws Exception;
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    public static void uncheck(CheckedOperation operation) {
        try {
            operation.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T uncheck(CheckedSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, R> Function<T, R> uncheck(CheckedFunction<T, R> checkedFunction) {
        return t -> {
            try {
                return checkedFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T, R> Optional<R> tryException(Function<T, R> function, T param) {
        try {
            return Optional.ofNullable(function.apply(param));
        } catch (Exception e) {
            log.error("invoke error", e);
            return Optional.empty();
        }
    }
}
