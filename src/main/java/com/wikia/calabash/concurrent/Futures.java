package com.wikia.calabash.concurrent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author feijianwu
 * @since 6/14/2021 7:45 PM
 */
public class Futures {

    public static CompletableFuture<?> allOfTerminateOnFailure(CompletableFuture<?>... futures) {
        CompletableFuture<?> failure = new CompletableFuture<>();
        for (CompletableFuture<?> f : futures) {
            f.exceptionally(ex -> {
                failure.completeExceptionally(ex);
                return null;
            });
        }

        failure.exceptionally(ex -> {
            Arrays.stream(futures).forEach(f -> f.cancel(true));
            return null;
        });
        return CompletableFuture.anyOf(failure, CompletableFuture.allOf(futures));
    }

    public static CompletableFuture<?> allOfTerminateOnFailure(List<CompletableFuture<?>> futures) {

        CompletableFuture<?> failure = new CompletableFuture<>();
        for (CompletableFuture<?> f : futures) {
            f.exceptionally(ex -> {
                failure.completeExceptionally(ex);
                return null;
            });
        }

        failure.exceptionally(ex -> {
            futures.forEach(f -> f.cancel(true));
            return null;
        });
        return CompletableFuture.anyOf(failure, CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])));
    }
}
