package com.wikia.calabash.algorithm;

import java.util.concurrent.Semaphore;

/**
 * @author wikia
 * @since 6/18/2021 5:16 PM
 */
public class D {

    public static void main(String[] args) {
        Semaphore[] semaphores = new Semaphore[10];
        semaphores[0] = new Semaphore(1);
        for (int i = 1; i < 10; i++) {
            semaphores[i] = new Semaphore(0);
        }

        for (int i = 0; i < 10; i++) {
            Semaphore cur = semaphores[i];
            Semaphore next = semaphores[i == 9 ? 0 : i + 1];
            new Thread(new Printer(cur, next, 100), "thread-" + (i + 1)).start();
        }
    }

    public static class Printer implements Runnable {
        private Semaphore cur;
        private Semaphore next;
        private int max;
        private static int i = 1;
        private volatile boolean run = true;

        public Printer(Semaphore cur, Semaphore next, int max) {
            this.cur = cur;
            this.next = next;
            this.max = max;
        }

        @Override
        public void run() {
            while (run) {
                try {
                    cur.acquire();
                    if (i <= max) {
                        System.out.println(Thread.currentThread().getName() + ":" + i++);
                    } else {
                        this.run = false;
                    }
                    next.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
