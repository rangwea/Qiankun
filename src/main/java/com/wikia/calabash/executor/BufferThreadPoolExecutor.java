package com.wikia.calabash.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

public class BufferThreadPoolExecutor implements Executor {
    private Semaphore bufferSemaphore;
    private Executor executor;

    /**
     * @param bufferSize buffer size
     * @param executor process Executor
     */
    public BufferThreadPoolExecutor(int bufferSize, Executor executor) {
        this.bufferSemaphore = new Semaphore(bufferSize);
        this.executor = executor;
    }

    @Override
    public void execute(final Runnable command) {
        try {
            // acquire, if no permit, blocking current thread
            bufferSemaphore.acquire();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        executor.execute(() -> {
            try {
                command.run();
            } finally {
                // release permit
                bufferSemaphore.release();
            }
        });
    }

}
