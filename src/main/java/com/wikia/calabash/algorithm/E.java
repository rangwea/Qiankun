package com.wikia.calabash.algorithm;

/**
 * @author wikia
 * @since 6/18/2021 5:41 PM
 */
public class E {

    public static void main(String[] args) {
        new Thread(new Printer(100), "threadA").start();
        new Thread(new Printer(100), "threadB").start();
    }

    public static class Printer implements Runnable {
        private static final Object monitor = new Object();
        private int max;

        private static int i = 0;

        public Printer(int max) {
            this.max = max;
        }

        @Override
        public void run() {
            while (i < max) {
                synchronized (monitor) {
                    monitor.notifyAll();
                    i++;
                    System.out.println(Thread.currentThread().getName() + ":" + i);
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            synchronized (monitor) {
                monitor.notifyAll();
            }
        }

    }


}
