package com.wikia.calabash.tolerant;

import org.aspectj.lang.ProceedingJoinPoint;


public interface TolerantHandler {
    void handle(ProceedingJoinPoint pjp, CatchTolerant catchTolerant);
}
