package com.wikia.calabash.algorithm;

import java.util.concurrent.Semaphore;

/**
 * @author wikia
 * @since 6/18/2021 7:54 PM
 */
public class F {
    private static Semaphore semaphore = new Semaphore(1);
    private static int index;

    public static void main(String[] args) {
        new Thread(() -> print(0), "thread-1").start();
        new Thread(() -> print(1), "thread-2").start();
        new Thread(() -> print(2), "thread-3").start();
    }

    private static void print(int i) {
        while (index < 100) {
            if (index % 3 == i) {
                try {
                    semaphore.acquire();
                    index++;
                    System.out.println(Thread.currentThread().getName() + ":" + index);
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
