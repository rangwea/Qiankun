package com.wikia.calabash.algorithm;

import java.util.concurrent.Semaphore;

/**
 * @author wikia
 * @since 6/15/2021 8:47 PM
 */
public class C {

    public static void main(String[] args) {
        Semaphore semaphoreA = new Semaphore(1);
        Semaphore semaphoreB = new Semaphore(0);
        Semaphore semaphoreC = new Semaphore(0);

        print("A", semaphoreA, semaphoreB);
        print("B", semaphoreB, semaphoreC);
        print("C", semaphoreC, semaphoreA);
    }

    public static void print(String letter, Semaphore cur, Semaphore next) {
        new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    cur.acquire();
                    System.out.println(letter);
                    next.release();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
