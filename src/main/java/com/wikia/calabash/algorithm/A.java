package com.wikia.calabash.algorithm;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wikia
 * @since 6/15/2021 3:35 PM
 */
public class A {

    public static void main(String[] args) {
        ReentrantLock lock = new ReentrantLock();
        Condition conditionA = lock.newCondition();
        Condition conditionB = lock.newCondition();
        Condition conditionC = lock.newCondition();

        print("A", lock, conditionA, conditionB);
        print("B", lock, conditionB, conditionC);
        print("C", lock, conditionC, conditionA);
    }

    public static void print(String letter, ReentrantLock lock, Condition cur, Condition next) {
        new Thread(() -> {
            try {
                lock.lock();
                for (int i = 0; i < 10; i++) {
                    System.out.println(letter);
                    next.signal();
                    cur.await();
                }
                next.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }).start();
    }
}
