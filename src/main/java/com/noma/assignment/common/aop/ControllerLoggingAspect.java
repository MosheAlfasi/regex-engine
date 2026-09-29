package com.noma.assignment.common.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect {

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String request = req.getMethod() + " " + req.getRequestURI() + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
        String action = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        long start = System.currentTimeMillis();

        log.info("Incoming request | {} | action: {}", request, action);
        try {
            Object result = joinPoint.proceed();
            log.info("Completed request | {} | action: {} | {} ms", request, action, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            log.info("Failed request | {} | action: {} | {} ms | {}: {}",
                    request, action, System.currentTimeMillis() - start, t.getClass().getSimpleName(), t.getMessage());
            throw t;
        }
    }
}
