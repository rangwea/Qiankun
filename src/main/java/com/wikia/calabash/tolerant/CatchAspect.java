package com.wikia.calabash.tolerant;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class CatchAspect {
    private TolerantHandler tolerantHandler = new LogTolerantHandler();

    @Around("@annotation(CatchTolerant)")
    public Object logAction(ProceedingJoinPoint pjp) throws Throwable {
        CatchTolerant catchTolerant = this.getAnnotation(pjp);
        if (catchTolerant == null) {
            return pjp.proceed();
        }

        try {
            return pjp.proceed();
        } catch (Throwable throwable) {
            log.error("", throwable);
            tolerantHandler.handle(pjp, catchTolerant);
        }
        return null;
    }

    private CatchTolerant getAnnotation(ProceedingJoinPoint pjp) {
        try {
            MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
            return methodSignature.getMethod().getAnnotation(CatchTolerant.class);
        } catch (Throwable e) {
            log.error("get ");
        }
        return null;
    }


}