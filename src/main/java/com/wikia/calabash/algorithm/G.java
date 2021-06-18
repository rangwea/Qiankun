package com.wikia.calabash.algorithm;

/**
 * @author wikia
 * @since 6/18/2021 7:54 PM
 */
public class G {
    private static final Object monitor = new Object();
    private static int index;

    public static void main(String[] args) {
        new Thread(() -> print(0), "thread-1").start();
        new Thread(() -> print(1), "thread-2").start();
        new Thread(() -> print(2), "thread-3").start();
    }

    private static void print(int i) {
        while (index < 100) {
            synchronized (monitor) {
                if (index % 3 == i) {
                    index++;
                    System.out.println(Thread.currentThread().getName() + ":" + index);
                    monitor.notifyAll();
                } else {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
