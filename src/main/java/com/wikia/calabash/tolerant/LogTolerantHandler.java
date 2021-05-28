package com.wikia.calabash.tolerant;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTolerantHandler implements TolerantHandler {
    private Logger logger = LoggerFactory.getLogger("tolerantLogger");

    @Override
    public void handle(ProceedingJoinPoint pjp, CatchTolerant catchTolerant) {
        Signature signature = pjp.getSignature();

        String message = catchTolerant.message();
        if ("".equals(message)) {
            message = signature.getDeclaringTypeName() + "." + signature.getName();
        }

        Object[] args = pjp.getArgs();
        StringBuilder print = new StringBuilder();
        print.append(message).append(":");

        for (Object arg : args) {
            print.append(arg);
        }

        logger.info(print.toString());
    }

}
