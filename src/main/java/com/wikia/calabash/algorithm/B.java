package com.wikia.calabash.algorithm;

import java.util.concurrent.locks.LockSupport;

/**
 * @author wikia
 * @since 6/15/2021 5:46 PM
 */
public class B {

    static Thread threadA, threadB , threadC;

    public static void main(String[] args) {
        threadA = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("A");
                // 唤醒下一个执行的线程
                LockSupport.unpark(threadB);
                // 阻塞当前线程，等待被唤醒
                LockSupport.park();
            }
        });

        threadB = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                // 阻塞当前线程，等待被唤醒
                LockSupport.park();
                System.out.println("B");
                // 唤醒下一个执行的线程
                LockSupport.unpark(threadC);
            }
        });

        threadC = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                // 阻塞当前线程，等待被唤醒
                LockSupport.park();
                System.out.println("C");
                // 唤醒下一个执行的线程
                LockSupport.unpark(threadA);
            }
        });

        threadA.start();
        threadB.start();
        threadC.start();
    }
}
